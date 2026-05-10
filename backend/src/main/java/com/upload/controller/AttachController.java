package com.upload.controller;

import com.upload.model.AttachResult;
import com.upload.model.ServiceParams;
import com.upload.model.UploadTask;
import com.upload.service.AttachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 提供文件上传的RESTful API接口
 *
 * @author system
 * @version 1.0.0
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttachController {
    
    private static final Logger logger = LoggerFactory.getLogger(AttachController.class);
    
    @Autowired
    private AttachService attachService;
    
    @PostMapping("/init")
    public AttachResult initUpload(@RequestBody ServiceParams serviceParams) {
        logger.info("初始化上传请求：fileName={}, fileSize={}", 
                serviceParams.getFileName(), serviceParams.getFileSize());
        
        try {
            return attachService.initUpload(serviceParams);
        } catch (Exception e) {
            logger.error("初始化上传失败", e);
            return AttachResult.error("初始化上传失败：" + e.getMessage());
        }
    }
    
    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachResult uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileCode", required = false) String fileCode,
            @RequestParam(value = "chunkNum", required = false) String chunkNum,
            @RequestParam(value = "chunks", required = false) String chunks,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "fileSize", required = false) String fileSize,
            @RequestParam(value = "fileMd5", required = false) String fileMd5,
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "pgmId", required = false) String pgmId,
            HttpServletRequest request) {
        
        logger.debug("分片上传请求：fileCode={}, chunkNum={}, chunks={}, fileSize={}", 
                fileCode, chunkNum, chunks, file != null ? file.getSize() : 0);
        
        if (fileCode == null || chunkNum == null) {
            fileCode = request.getParameter("fileCode");
            chunkNum = request.getParameter("chunkNum");
            chunks = request.getParameter("chunks");
        }
        
        ServiceParams serviceParams = new ServiceParams();
        serviceParams.setFileCode(fileCode);
        serviceParams.setChunkNum(chunkNum);
        serviceParams.setChunks(chunks);
        serviceParams.setFileName(fileName);
        serviceParams.setFileSize(fileSize);
        serviceParams.setFileMd5(fileMd5);
        serviceParams.setTableName(tableName);
        serviceParams.setPgmId(pgmId);
        
        try {
            return attachService.uploadChunk(serviceParams, file);
        } catch (Exception e) {
            logger.error("分片上传失败", e);
            return AttachResult.error("分片上传失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/merge")
    public AttachResult mergeChunks(@RequestParam String fileCode) {
        logger.info("合并分片请求：fileCode={}", fileCode);
        
        try {
            return attachService.mergeChunks(fileCode);
        } catch (Exception e) {
            logger.error("合并分片失败", e);
            return AttachResult.error("合并分片失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/status/{fileCode}")
    public AttachResult getUploadStatus(@PathVariable String fileCode) {
        logger.debug("查询上传状态：fileCode={}", fileCode);
        
        try {
            UploadTask task = attachService.getUploadStatus(fileCode);
            
            if (task == null) {
                return AttachResult.error("上传任务不存在");
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileCode", task.getFileCode());
            data.put("fileName", task.getFileName());
            data.put("fileSize", task.getFileSize());
            data.put("status", task.getStatus());
            data.put("uploadedChunks", task.getUploadedChunks());
            data.put("totalChunks", task.getTotalChunks());
            data.put("progress", task.getProgress());
            data.put("filePath", task.getFilePath());
            
            return AttachResult.success(data);
            
        } catch (Exception e) {
            logger.error("查询上传状态失败", e);
            return AttachResult.error("查询上传状态失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/cancel")
    public AttachResult cancelUpload(@RequestParam String fileCode) {
        logger.info("取消上传请求：fileCode={}", fileCode);
        
        try {
            boolean success = attachService.cancelUpload(fileCode);
            
            if (success) {
                return AttachResult.success("上传已取消");
            } else {
                return AttachResult.error("取消上传失败");
            }
            
        } catch (Exception e) {
            logger.error("取消上传失败", e);
            return AttachResult.error("取消上传失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/delete")
    public AttachResult deleteUpload(@RequestParam String fileCode) {
        logger.info("删除上传请求：fileCode={}", fileCode);
        
        try {
            boolean success = attachService.deleteUpload(fileCode);
            
            if (success) {
                return AttachResult.success("删除成功");
            } else {
                return AttachResult.error("删除失败");
            }
            
        } catch (Exception e) {
            logger.error("删除上传失败", e);
            return AttachResult.error("删除上传失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public String test() {
        return "File Upload Service is running!";
    }
}
