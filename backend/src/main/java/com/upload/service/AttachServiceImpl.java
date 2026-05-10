package com.upload.service;

import com.upload.model.AttachResult;
import com.upload.model.ServiceParams;
import com.upload.model.UploadTask;
import com.upload.repository.UploadTaskRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 文件上传服务实现类
 * 实现分片上传的核心业务逻辑
 *
 * @author system
 * @version 1.0.0
 */
@Service
public class AttachServiceImpl implements AttachService {
    
    private static final Logger logger = LoggerFactory.getLogger(AttachServiceImpl.class);
    
    @Autowired
    private UploadTaskRepository taskRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${upload.chunk-size:5242880}")
    private int defaultChunkSize;
    
    @Value("${upload.max-file-size:2147483648}")
    private long maxFileSize;
    
    @Value("${upload.cleanup-interval:86400000}")
    private long cleanupInterval;
    
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            "zip", "rar", "7z", "tar", "gz",
            "mp3", "mp4", "avi", "mov", "wmv", "flv",
            "js", "css", "html", "json", "xml"
    ));
    
    @Override
    public AttachResult initUpload(ServiceParams serviceParams) {
        if (serviceParams == null) {
            return AttachResult.error("参数不能为空");
        }
        
        String fileName = serviceParams.getFileName();
        String fileSizeStr = serviceParams.getFileSize();
        String fileMd5 = serviceParams.getFileMd5();
        String chunkSizeStr = serviceParams.getChunkSize();
        String tableName = serviceParams.getTableName();
        String pgmId = serviceParams.getPgmId();
        
        if (StringUtils.isBlank(fileName)) {
            return AttachResult.error("文件名不能为空");
        }
        
        if (StringUtils.isBlank(fileSizeStr)) {
            return AttachResult.error("文件大小不能为空");
        }
        
        long fileSize;
        try {
            fileSize = Long.parseLong(fileSizeStr);
        } catch (NumberFormatException e) {
            return AttachResult.error("文件大小格式错误");
        }
        
        if (fileSize <= 0) {
            return AttachResult.error("文件大小必须大于0");
        }
        
        if (fileSize > maxFileSize) {
            return AttachResult.error("文件大小超过限制（最大2GB）");
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return AttachResult.error("不支持的文件类型：" + extension);
        }
        
        int chunkSize = defaultChunkSize;
        if (StringUtils.isNotBlank(chunkSizeStr)) {
            try {
                chunkSize = Integer.parseInt(chunkSizeStr);
            } catch (NumberFormatException e) {
                logger.warn("分片大小格式错误，使用默认值");
            }
        }
        
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
        
        String fileCode = generateFileCode();
        
        UploadTask task = new UploadTask();
        task.setFileCode(fileCode);
        task.setFileName(fileName);
        task.setFileSize(fileSize);
        task.setFileMd5(fileMd5);
        task.setChunkSize(chunkSize);
        task.setTotalChunks(totalChunks);
        task.setStatus("uploading");
        task.setTableName(tableName);
        task.setPgmId(pgmId);
        task.setUploadedChunks(new ArrayList<>());
        
        taskRepository.save(task);
        
        logger.info("初始化上传任务成功：fileCode={}, fileName={}, totalChunks={}", 
                fileCode, fileName, totalChunks);
        
        Map<String, Object> data = new HashMap<>();
        data.put("fileCode", fileCode);
        data.put("chunks", totalChunks);
        data.put("chunkSize", chunkSize);
        data.put("uploadedChunks", Collections.emptyList());
        
        return AttachResult.success("初始化成功", data);
    }
    
    @Override
    public AttachResult uploadChunk(ServiceParams serviceParams, MultipartFile file) {
        if (serviceParams == null || file == null) {
            return AttachResult.error("参数不能为空");
        }
        
        String fileCode = serviceParams.getFileCode();
        String chunkNumStr = serviceParams.getChunkNum();
        String chunksStr = serviceParams.getChunks();
        
        if (StringUtils.isBlank(fileCode)) {
            return AttachResult.error("文件编码不能为空");
        }
        
        if (StringUtils.isBlank(chunkNumStr)) {
            return AttachResult.error("分片编号不能为空");
        }
        
        UploadTask task = taskRepository.findByFileCode(fileCode);
        if (task == null) {
            return AttachResult.error("上传任务不存在：" + fileCode);
        }
        
        int chunkNum;
        try {
            chunkNum = Integer.parseInt(chunkNumStr);
        } catch (NumberFormatException e) {
            return AttachResult.error("分片编号格式错误");
        }
        
        if (chunkNum < 0 || chunkNum >= task.getTotalChunks()) {
            return AttachResult.error("分片编号超出范围");
        }
        
        try {
            boolean saved = fileStorageService.saveChunk(fileCode, chunkNum, file.getInputStream());
            
            if (!saved) {
                return AttachResult.error("分片保存失败");
            }
            
            task.addUploadedChunk(chunkNum);
            task.setUpdateTime(LocalDateTime.now());
            taskRepository.update(task);
            
            logger.info("分片上传成功：fileCode={}, chunkNum={}, totalChunks={}", 
                    fileCode, chunkNum, task.getTotalChunks());
            
            Map<String, Object> data = new HashMap<>();
            data.put("chunkNum", chunkNum);
            data.put("chunkSize", file.getSize());
            data.put("uploadedChunks", task.getUploadedChunks());
            data.put("progress", task.getProgress());
            
            return AttachResult.success("分片上传成功", data);
            
        } catch (Exception e) {
            logger.error("分片上传失败：fileCode=" + fileCode + ", chunkNum=" + chunkNum, e);
            return AttachResult.error("分片上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public AttachResult mergeChunks(String fileCode) {
        if (StringUtils.isBlank(fileCode)) {
            return AttachResult.error("文件编码不能为空");
        }
        
        UploadTask task = taskRepository.findByFileCode(fileCode);
        if (task == null) {
            return AttachResult.error("上传任务不存在：" + fileCode);
        }
        
        if (!task.isAllChunksUploaded()) {
            int missing = task.getTotalChunks() - task.getUploadedChunks().size();
            return AttachResult.error("还有 " + missing + " 个分片未上传");
        }
        
        String filePath = fileStorageService.mergeChunks(fileCode, task.getTotalChunks(), task.getFileName());
        
        if (filePath == null) {
            task.setStatus("failed");
            task.setUpdateTime(LocalDateTime.now());
            taskRepository.update(task);
            return AttachResult.error("分片合并失败");
        }
        
        task.setFilePath(filePath);
        task.setStatus("completed");
        task.setUpdateTime(LocalDateTime.now());
        taskRepository.update(task);
        
        logger.info("分片合并成功：fileCode={}, filePath={}", fileCode, filePath);
        
        Map<String, Object> data = new HashMap<>();
        data.put("filePath", filePath);
        data.put("fileSize", task.getFileSize());
        data.put("fileName", task.getFileName());
        
        return AttachResult.success("文件上传完成", data);
    }
    
    @Override
    public UploadTask getUploadStatus(String fileCode) {
        if (StringUtils.isBlank(fileCode)) {
            return null;
        }
        return taskRepository.findByFileCode(fileCode);
    }
    
    @Override
    public boolean cancelUpload(String fileCode) {
        if (StringUtils.isBlank(fileCode)) {
            return false;
        }
        
        fileStorageService.deleteChunks(fileCode);
        
        UploadTask task = taskRepository.findByFileCode(fileCode);
        if (task != null) {
            task.setStatus("cancelled");
            task.setUpdateTime(LocalDateTime.now());
            taskRepository.update(task);
        }
        
        logger.info("取消上传任务：fileCode={}", fileCode);
        return true;
    }
    
    @Override
    public boolean deleteUpload(String fileCode) {
        if (StringUtils.isBlank(fileCode)) {
            return false;
        }
        
        fileStorageService.deleteChunks(fileCode);
        
        boolean deleted = taskRepository.deleteByFileCode(fileCode);
        
        if (deleted) {
            logger.info("删除上传任务：fileCode={}", fileCode);
        }
        
        return deleted;
    }
    
    @Scheduled(fixedRateString = "${upload.cleanup-interval:86400000}")
    public void cleanupExpiredTasks() {
        logger.info("开始清理过期上传任务");
        fileStorageService.cleanupExpiredChunks(cleanupInterval);
        
        List<UploadTask> pendingTasks = taskRepository.findByStatus("uploading");
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
        
        for (UploadTask task : pendingTasks) {
            if (task.getUpdateTime().isBefore(expireTime)) {
                logger.info("清理过期任务：fileCode={}", task.getFileCode());
                cancelUpload(task.getFileCode());
            }
        }
        
        logger.info("清理过期上传任务完成");
    }
    
    private String generateFileCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
    
    private String getFileExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }
}
