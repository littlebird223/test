package com.upload.model;

import java.io.Serializable;

/**
 * 文件上传结果类
 * 用于封装文件上传操作的结果信息
 *
 * @author system
 * @version 1.0.0
 */
public class AttachResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    
    private String message;
    
    private Object data;
    
    public AttachResult() {
        this.success = false;
        this.message = "";
    }
    
    public AttachResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AttachResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static AttachResult success() {
        return new AttachResult(true, "操作成功");
    }
    
    public static AttachResult success(String message) {
        return new AttachResult(true, message);
    }
    
    public static AttachResult success(Object data) {
        return new AttachResult(true, "操作成功", data);
    }
    
    public static AttachResult success(String message, Object data) {
        return new AttachResult(true, message, data);
    }
    
    public static AttachResult error(String message) {
        return new AttachResult(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}
