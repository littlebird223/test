package com.upload.model;

/**
 * 对象存储配置模型
 * 用于封装对象存储服务的配置信息（HOST、AccessKey、SecretKey、Bucket等）
 * 
 * @author system
 * @version 1.0.0
 */
public class ObjectStorageModel {
    
    private String hostAdr;
    
    private String accessKey;
    
    private String secretKey;
    
    private String bucketSht;
    
    public ObjectStorageModel() {
    }
    
    public ObjectStorageModel(String hostAdr, String accessKey, String secretKey, String bucketSht) {
        this.hostAdr = hostAdr;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketSht = bucketSht;
    }
    
    public String getHostAdr() {
        return hostAdr;
    }
    
    public void setHostAdr(String hostAdr) {
        this.hostAdr = hostAdr;
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public String getBucketSht() {
        return bucketSht;
    }
    
    public void setBucketSht(String bucketSht) {
        this.bucketSht = bucketSht;
    }
    
    public String getBucketName() {
        return bucketSht;
    }
}
