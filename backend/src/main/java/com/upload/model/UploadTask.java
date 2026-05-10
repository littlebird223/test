package com.upload.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传任务实体类
 * 用于记录和管理文件上传任务信息
 *
 * @author system
 * @version 1.0.0
 */
public class UploadTask implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String fileCode;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileMd5;
    
    private Integer chunkSize;
    
    private Integer totalChunks;
    
    private List<Integer> uploadedChunks;
    
    private String status;
    
    private String filePath;
    
    private String tableName;
    
    private String pgmId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    public UploadTask() {
        this.uploadedChunks = new ArrayList<>();
        this.status = "pending";
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFileCode() {
        return fileCode;
    }
    
    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileMd5() {
        return fileMd5;
    }
    
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
    
    public Integer getChunkSize() {
        return chunkSize;
    }
    
    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
    
    public Integer getTotalChunks() {
        return totalChunks;
    }
    
    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }
    
    public List<Integer> getUploadedChunks() {
        return uploadedChunks;
    }
    
    public void setUploadedChunks(List<Integer> uploadedChunks) {
        this.uploadedChunks = uploadedChunks;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getPgmId() {
        return pgmId;
    }
    
    public void setPgmId(String pgmId) {
        this.pgmId = pgmId;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public void addUploadedChunk(Integer chunkNum) {
        if (!this.uploadedChunks.contains(chunkNum)) {
            this.uploadedChunks.add(chunkNum);
            this.updateTime = LocalDateTime.now();
        }
    }
    
    public boolean isAllChunksUploaded() {
        return this.uploadedChunks.size() >= this.totalChunks;
    }
    
    public int getProgress() {
        if (this.totalChunks == null || this.totalChunks == 0) {
            return 0;
        }
        return (int) ((this.uploadedChunks.size() * 100.0) / this.totalChunks);
    }
}
