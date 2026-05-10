package com.upload.model;

/**
 * 文件下载参数类
 * 用于封装文件下载操作的参数信息
 *
 * @author system
 * @version 1.0.0
 */
public class DownloadParam {
    
    private Long startSign;
    
    private Long chunkNum;
    
    private boolean resume;
    
    public DownloadParam() {
    }
    
    public DownloadParam(Long startSign, Long chunkNum, boolean resume) {
        this.startSign = startSign;
        this.chunkNum = chunkNum;
        this.resume = resume;
    }
    
    public Long getStartSign() {
        return startSign;
    }
    
    public void setStartSign(Long startSign) {
        this.startSign = startSign;
    }
    
    public Long getChunkNum() {
        return chunkNum;
    }
    
    public void setChunkNum(Long chunkNum) {
        this.chunkNum = chunkNum;
    }
    
    public boolean isResume() {
        return resume;
    }
    
    public void setResume(boolean resume) {
        this.resume = resume;
    }
}
