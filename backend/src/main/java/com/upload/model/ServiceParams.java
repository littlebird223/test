package com.upload.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务参数类
 * 用于封装前后端交互的参数信息
 *
 * @author system
 * @version 1.0.0
 */
public class ServiceParams {
    
    private Map<String, String> parameters;
    
    public ServiceParams() {
        this.parameters = new HashMap<>();
    }
    
    public ServiceParams(Map<String, String> parameters) {
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }
    
    public String getParameter(String key) {
        return parameters.get(key);
    }
    
    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String getFileName() {
        return getParameter("fileName");
    }
    
    public void setFileName(String fileName) {
        setParameter("fileName", fileName);
    }
    
    public String getFileSize() {
        return getParameter("fileSize");
    }
    
    public void setFileSize(String fileSize) {
        setParameter("fileSize", fileSize);
    }
    
    public String getFileMd5() {
        return getParameter("fileMd5");
    }
    
    public void setFileMd5(String fileMd5) {
        setParameter("fileMd5", fileMd5);
    }
    
    public String getChunkSize() {
        return getParameter("chunkSize");
    }
    
    public void setChunkSize(String chunkSize) {
        setParameter("chunkSize", chunkSize);
    }
    
    public String getTableName() {
        return getParameter("tableName");
    }
    
    public void setTableName(String tableName) {
        setParameter("tableName", tableName);
    }
    
    public String getPgmId() {
        return getParameter("pgmId");
    }
    
    public void setPgmId(String pgmId) {
        setParameter("pgmId", pgmId);
    }
    
    public String getFileCode() {
        return getParameter("fileCode");
    }
    
    public void setFileCode(String fileCode) {
        setParameter("fileCode", fileCode);
    }
    
    public String getChunkNum() {
        return getParameter("chunkNum");
    }
    
    public void setChunkNum(String chunkNum) {
        setParameter("chunkNum", chunkNum);
    }
    
    public String getChunks() {
        return getParameter("chunks");
    }
    
    public void setChunks(String chunks) {
        setParameter("chunks", chunks);
    }
}
