package com.upload.business;

import com.upload.model.AttachParam;
import com.upload.model.AttachResult;
import com.upload.model.ObjectStorageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据平台混合业务类
 * 实现 AttachBusiness 接口，提供混合上传策略：
 * - 小文件（≤100MB）：使用 V2 流式上传
 * - 大文件（>100MB）：使用 V1 磁盘暂存 + 合并上传
 * 
 * 仿照阿里云OSS写法设计，支持分片上传和断点续传
 * 
 * @author system
 * @version 1.0.0
 */
@Component
public class DataPlatformMixBusiness implements AttachBusiness {

    private static final Logger logger = LoggerFactory.getLogger(DataPlatformMixBusiness.class);

    private final ObjectStorageModel objectStorageModel;
    private final DataPlatformFileClient client = new DataPlatformFileClient();

    private static final String TMP_DIR = "./uploads/temp/chunks/";
    private final Map<String, Integer> chunkCounter = new ConcurrentHashMap<>();
    public static final long MAX_STREAM_SIZE = 100 * 1024 * 1024L; // 100M

    public DataPlatformMixBusiness(ObjectStorageModel objectStorageModel) {
        this.objectStorageModel = objectStorageModel;
        initTempDirectory();
    }

    private void initTempDirectory() {
        try {
            Path tempPath = Paths.get(TMP_DIR);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
                logger.info("创建分片临时目录: {}", TMP_DIR);
            }
        } catch (IOException e) {
            logger.error("创建分片临时目录失败", e);
        }
    }

    public static DataPlatformMixBusiness getInstanceByAppId(String appId) {
        ObjectStorageModel model = new ObjectStorageModel(
                "http://localhost:8080",
                "your-token-here",
                "your-secret-here",
                "default-bucket"
        );
        return new DataPlatformMixBusiness(model);
    }

    @Override
    public AttachResult upload(AttachParam param) {
        try {
            if (param.getFileSize() == null) {
                AttachResult res = new AttachResult();
                res.setSuccess(false);
                res.setMessage("文件大小不能为空");
                return res;
            }

            long fileSize = Long.parseLong(param.getFileSize());
            logger.info("开始上传文件: fileCode={}, fileName={}, fileSize={}", 
                    param.getFileCode(), param.getName(), fileSize);

            if (fileSize <= MAX_STREAM_SIZE) {
                return uploadStream(param);
            } else {
                return uploadDisk(param);
            }
        } catch (Exception e) {
            logger.error("混合上传异常: fileCode={}", param.getFileCode(), e);
            AttachResult res = new AttachResult();
            res.setSuccess(false);
            res.setMessage("混合上传异常: " + e.getMessage());
            return res;
        }
    }

    private AttachResult uploadStream(AttachParam param) {
        logger.info("使用V2流式上传: fileCode={}", param.getFileCode());
        AttachResult result = new AttachResult();
        
        try (InputStream in = param.getInputStream()) {
            return client.uploadStreamV2(
                    objectStorageModel.getBucketName(),
                    in,
                    param.getName(),
                    param.getFilePath(),
                    0,
                    param.getFileCode(),
                    objectStorageModel.getAccessKey()
            );
        } catch (Exception e) {
            logger.error("V2流式上传失败: fileCode={}", param.getFileCode(), e);
            result.setSuccess(false);
            result.setMessage("V2流式上传失败: " + e.getMessage());
            return result;
        }
    }

    private AttachResult uploadDisk(AttachParam param) throws Exception {
        String fileCode = param.getFileCode();
        int chunkNo = Integer.parseInt(param.getChunkNo());
        int totalChunks = Integer.parseInt(param.getChunkQty());

        logger.info("使用V1磁盘分片上传: fileCode={}, chunkNo={}/{}", 
                fileCode, chunkNo, totalChunks);

        String chunkPath = TMP_DIR + fileCode + "_" + chunkNo + ".part";
        try (InputStream in = param.getInputStream();
             FileOutputStream fos = new FileOutputStream(chunkPath)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
        }

        int received = chunkCounter.merge(fileCode, 1, Integer::sum);
        logger.info("已接收分片: fileCode={}, received={}/{}", fileCode, received, totalChunks);

        if (received < totalChunks) {
            AttachResult res = new AttachResult();
            res.setSuccess(true);
            res.setMessage("分片上传成功，等待其他分片");
            return res;
        }

        logger.info("所有分片已接收，开始合并: fileCode={}", fileCode);
        SequenceInputStream sis = mergeChunks(fileCode, totalChunks);

        AttachResult result = client.upload(
                objectStorageModel.getBucketName(),
                sis,
                param.getFilePath(),
                0,
                objectStorageModel.getAccessKey(),
                fileCode
        );

        if (result.isSuccess()) {
            logger.info("分片合并上传成功: fileCode={}", fileCode);
            deleteChunks(fileCode, totalChunks);
            chunkCounter.remove(fileCode);
        } else {
            logger.error("分片合并上传失败: fileCode={}, message={}", fileCode, result.getMessage());
        }
        
        return result;
    }

    private SequenceInputStream mergeChunks(String fileCode, int total) throws Exception {
        SequenceInputStream sis = null;
        for (int i = 0; i < total; i++) {
            String chunkPath = TMP_DIR + fileCode + "_" + i + ".part";
            File chunkFile = new File(chunkPath);
            
            if (!chunkFile.exists()) {
                throw new FileNotFoundException("分片文件不存在: " + chunkPath);
            }
            
            InputStream in = Files.newInputStream(chunkFile.toPath());
            if (sis == null) {
                sis = new SequenceInputStream(in, new ByteArrayInputStream(new byte[0]));
            } else {
                sis = new SequenceInputStream(sis, in);
            }
        }
        return sis;
    }

    private void deleteChunks(String fileCode, int total) {
        for (int i = 0; i < total; i++) {
            try {
                Path chunkPath = Paths.get(TMP_DIR + fileCode + "_" + i + ".part");
                Files.deleteIfExists(chunkPath);
            } catch (Exception e) {
                logger.warn("删除分片失败: fileCode={}, chunkNo={}", fileCode, i);
            }
        }
    }

    @Override
    public boolean download(AttachParam param) {
        if (param.getOutputStream() == null) {
            return false;
        }
        
        try {
            InputStream in = downloadAsStream(param);
            if (in == null) {
                return false;
            }
            
            try (OutputStream out = param.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("下载失败: fileCode={}", param.getFileCode(), e);
            return false;
        }
    }

    @Override
    public InputStream downloadAsStream(AttachParam param) {
        try {
            String downloadUrl = client.getDownloadUrl(
                    objectStorageModel.getBucketName(),
                    param.getFileCode(),
                    objectStorageModel.getAccessKey()
            );
            
            java.net.URL url = new java.net.URL(downloadUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + objectStorageModel.getAccessKey());
            
            if (conn.getResponseCode() == 200) {
                return conn.getInputStream();
            }
        } catch (Exception e) {
            logger.error("获取下载流失败: fileCode={}", param.getFileCode(), e);
        }
        return null;
    }

    @Override
    public boolean checkConnection() {
        try {
            java.net.URL url = new java.net.URL(objectStorageModel.getBucketName() + "/health");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            logger.warn("连接检查失败", e);
            return true;
        }
    }
}
