package com.upload.model;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件上传参数类
 * 用于在上传流程中传递各种参数信息
 *
 * @author system
 * @version 1.0.0
 */
public class AttachParam {
    
    private String appId;
    
    private String fileCode;
    
    private InputStream inputStream;
    
    private OutputStream outputStream;
    
    private String filePath;
    
    private String chunkNo;
    
    private String chunkQty;
    
    private String fileSta;
    
    private boolean isExist;
    
    private int lastChunkNo;
    
    private DownloadParam downloadParam;
    
    private String fileExtNam;
    
    private String fileTypId;
    
    private String fileSize;
    
    private String fileMd5;
    
    private String chunkSize;
    
    private String name;
    
    private boolean isResume;
    
    public AttachParam() {
    }
    
    public AttachParam(String fileCode) {
        this.fileCode = fileCode;
    }
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public AttachParam appId(String appId) {
        this.appId = appId;
        return this;
    }
    
    public String getFileCode() {
        return fileCode;
    }
    
    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }
    
    public AttachParam fileCode(String fileCode) {
        this.fileCode = fileCode;
        return this;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    public AttachParam inputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }
    
    public OutputStream getOutputStream() {
        return outputStream;
    }
    
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
    
    public AttachParam outputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public AttachParam filePath(String filePath) {
        this.filePath = filePath;
        return this;
    }
    
    public String getChunkNo() {
        return chunkNo;
    }
    
    public void setChunkNo(String chunkNo) {
        this.chunkNo = chunkNo;
    }
    
    public AttachParam chunkNo(String chunkNo) {
        this.chunkNo = chunkNo;
        return this;
    }
    
    public String getChunkQty() {
        return chunkQty;
    }
    
    public void setChunkQty(String chunkQty) {
        this.chunkQty = chunkQty;
    }
    
    public AttachParam chunkQty(String chunkQty) {
        this.chunkQty = chunkQty;
        return this;
    }
    
    public String getFileSta() {
        return fileSta;
    }
    
    public void setFileSta(String fileSta) {
        this.fileSta = fileSta;
    }
    
    public boolean isExist() {
        return isExist;
    }
    
    public void setExist(boolean exist) {
        isExist = exist;
    }
    
    public int getLastChunkNo() {
        return lastChunkNo;
    }
    
    public void setLastChunkNo(int lastChunkNo) {
        this.lastChunkNo = lastChunkNo;
    }
    
    public DownloadParam getDownloadParam() {
        return downloadParam;
    }
    
    public void setDownloadParam(DownloadParam downloadParam) {
        this.downloadParam = downloadParam;
    }
    
    public String getFileExtNam() {
        return fileExtNam;
    }
    
    public void setFileExtNam(String fileExtNam) {
        this.fileExtNam = fileExtNam;
    }
    
    public AttachParam fileExtNam(String fileExtNam) {
        this.fileExtNam = fileExtNam;
        return this;
    }
    
    public String getFileTypId() {
        return fileTypId;
    }
    
    public void setFileTypId(String fileTypId) {
        this.fileTypId = fileTypId;
    }
    
    public String getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
    
    public AttachParam fileSize(String fileSize) {
        this.fileSize = fileSize;
        return this;
    }
    
    public String getFileMd5() {
        return fileMd5;
    }
    
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
    
    public AttachParam fileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
        return this;
    }
    
    public String getChunkSize() {
        return chunkSize;
    }
    
    public void setChunkSize(String chunkSize) {
        this.chunkSize = chunkSize;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public AttachParam name(String name) {
        this.name = name;
        return this;
    }
    
    public boolean isResume() {
        return isResume;
    }
    
    public void setResume(boolean resume) {
        isResume = resume;
    }
    
    public AttachParam resume(boolean isResume) {
        this.isResume = isResume;
        return this;
    }
}
