package com.upload.service;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 本地文件系统存储服务实现类
 * 实现文件的分片存储、合并和清理功能
 *
 * @author system
 * @version 1.0.0
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    
    @Value("${upload.temp-path:./uploads/temp}")
    private String tempPath;
    
    @Value("${upload.file-path:./uploads/files}")
    private String filePath;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");
    
    @Override
    public boolean saveChunk(String fileCode, int chunkNum, InputStream inputStream) {
        if (!StringUtils.hasText(fileCode) || inputStream == null) {
            logger.error("保存分片失败：文件编码或输入流为空");
            return false;
        }
        
        try {
            Path chunkDir = Paths.get(tempPath, fileCode);
            if (!Files.exists(chunkDir)) {
                Files.createDirectories(chunkDir);
            }
            
            Path chunkFile = chunkDir.resolve(fileCode + "_" + chunkNum);
            
            try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(chunkFile))) {
                IOUtils.copy(inputStream, outputStream);
            }
            
            logger.info("分片保存成功：fileCode={}, chunkNum={}", fileCode, chunkNum);
            return true;
            
        } catch (IOException e) {
            logger.error("保存分片失败：fileCode=" + fileCode + ", chunkNum=" + chunkNum, e);
            return false;
        }
    }
    
    @Override
    public InputStream getChunk(String fileCode, int chunkNum) {
        if (!StringUtils.hasText(fileCode)) {
            return null;
        }
        
        try {
            Path chunkFile = Paths.get(tempPath, fileCode, fileCode + "_" + chunkNum);
            
            if (Files.exists(chunkFile)) {
                return new BufferedInputStream(Files.newInputStream(chunkFile));
            }
            
        } catch (IOException e) {
            logger.error("获取分片失败：fileCode=" + fileCode + ", chunkNum=" + chunkNum, e);
        }
        
        return null;
    }
    
    @Override
    public String mergeChunks(String fileCode, int totalChunks, String fileName) {
        if (!StringUtils.hasText(fileCode) || totalChunks <= 0 || !StringUtils.hasText(fileName)) {
            logger.error("合并分片失败：参数不合法");
            return null;
        }
        
        String sanitizedFileName = sanitizeFileName(fileName);
        Path chunkDir = Paths.get(tempPath, fileCode);
        
        if (!Files.exists(chunkDir)) {
            logger.error("合并分片失败：分片目录不存在");
            return null;
        }
        
        try {
            LocalDate now = LocalDate.now();
            String relativePath = now.format(DATE_FORMATTER) + "/" + fileCode + "_" + sanitizedFileName;
            Path targetDir = Paths.get(filePath, now.format(DATE_FORMATTER));
            
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            
            Path targetFile = Paths.get(filePath, relativePath);
            
            try (OutputStream mergedOutput = new BufferedOutputStream(Files.newOutputStream(targetFile))) {
                for (int i = 0; i < totalChunks; i++) {
                    Path chunkFile = chunkDir.resolve(fileCode + "_" + i);
                    
                    if (!Files.exists(chunkFile)) {
                        logger.error("合并分片失败：分片文件不存在 index={}", i);
                        Files.deleteIfExists(targetFile);
                        return null;
                    }
                    
                    try (InputStream chunkInput = new BufferedInputStream(Files.newInputStream(chunkFile))) {
                        IOUtils.copy(chunkInput, mergedOutput);
                    }
                }
            }
            
            logger.info("分片合并成功：fileCode={}, totalChunks={}, targetPath={}", 
                    fileCode, totalChunks, relativePath);
            
            deleteChunks(fileCode);
            
            return relativePath;
            
        } catch (IOException e) {
            logger.error("合并分片失败：fileCode=" + fileCode, e);
            return null;
        }
    }
    
    @Override
    public boolean deleteChunks(String fileCode) {
        if (!StringUtils.hasText(fileCode)) {
            return false;
        }
        
        try {
            Path chunkDir = Paths.get(tempPath, fileCode);
            
            if (Files.exists(chunkDir)) {
                try (Stream<Path> paths = Files.walk(chunkDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    logger.warn("删除分片文件失败：{}", path);
                                }
                            });
                }
                logger.info("分片文件删除成功：fileCode={}", fileCode);
            }
            
            return true;
            
        } catch (IOException e) {
            logger.error("删除分片文件失败：fileCode=" + fileCode, e);
            return false;
        }
    }
    
    @Override
    public boolean chunkExists(String fileCode, int chunkNum) {
        if (!StringUtils.hasText(fileCode)) {
            return false;
        }
        
        Path chunkFile = Paths.get(tempPath, fileCode, fileCode + "_" + chunkNum);
        return Files.exists(chunkFile);
    }
    
    @Override
    public String getChunkDirectory(String fileCode) {
        if (!StringUtils.hasText(fileCode)) {
            return null;
        }
        return Paths.get(tempPath, fileCode).toString();
    }
    
    @Override
    public void cleanupExpiredChunks(long maxAge) {
        try {
            Path tempDir = Paths.get(tempPath);
            
            if (!Files.exists(tempDir)) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            
            try (Stream<Path> paths = Files.list(tempDir)) {
                paths.filter(Files::isDirectory)
                        .filter(path -> {
                            try {
                                long fileTime = Files.getLastModifiedTime(path).toMillis();
                                return (currentTime - fileTime) > maxAge;
                            } catch (IOException e) {
                                return false;
                            }
                        })
                        .forEach(path -> {
                            String fileCode = path.getFileName().toString();
                            logger.info("清理过期分片文件：fileCode={}", fileCode);
                            deleteChunks(fileCode);
                        });
            }
            
        } catch (IOException e) {
            logger.error("清理过期分片文件失败", e);
        }
    }
    
    /**
     * 清理文件名中的非法字符
     *
     * @param fileName 原始文件名
     * @return 清理后的文件名
     */
    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unknown";
        }
        
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        return sanitized.length() > 100 ? sanitized.substring(0, 100) : sanitized;
    }
    
    /**
     * 初始化存储目录
     */
    @javax.annotation.PostConstruct
    public void init() {
        try {
            Path tempDir = Paths.get(tempPath);
            Path fileDir = Paths.get(filePath);
            
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            
            if (!Files.exists(fileDir)) {
                Files.createDirectories(fileDir);
            }
            
            logger.info("存储目录初始化完成：tempPath={}, filePath={}", tempPath, filePath);
            
        } catch (IOException e) {
            logger.error("存储目录初始化失败", e);
        }
    }
}
