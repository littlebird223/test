package com.upload.business;

import com.upload.model.AttachResult;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 数据平台文件客户端
 * 提供文件上传的HTTP接口调用能力
 * 
 * @author system
 * @version 1.0.0
 */
public class DataPlatformFileClient {

    /**
     * 原有上传方法（V1版本）
     * 使用完整流上传，适用于合并后的分片文件
     *
     * @param host        服务器地址
     * @param fileStream  文件输入流
     * @param directory   存储目录
     * @param shareMode   共享模式
     * @param token       认证令牌
     * @param id          文件标识
     * @return 上传结果
     */
    public AttachResult upload(String host, InputStream fileStream, String directory, 
                              Integer shareMode, String token, String id) {
        AttachResult result = new AttachResult();
        HttpURLConnection conn = null;
        OutputStream out = null;
        
        try {
            URL url = new URL(host + "/fileapi/api/v1/files/upload");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("X-Directory", directory != null ? directory : "");
            conn.setRequestProperty("X-File-Id", id != null ? id : "");
            if (shareMode != null) {
                conn.setRequestProperty("X-Share-Mode", shareMode.toString());
            }
            
            out = conn.getOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = fileStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream respIn = conn.getInputStream()) {
                    String resp = new String(respIn.readAllBytes());
                    JSONObject jo = JSONObject.parseObject(resp);
                    if (jo.getIntValue("code") == 200) {
                        result.setSuccess(true);
                        result.setCallbackData(jo.getJSONObject("data").getString("fileKey"));
                    } else {
                        result.setMessage(jo.getString("message"));
                    }
                }
            } else {
                result.setMessage("上传失败，HTTP状态码：" + responseCode);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("上传异常：" + e.getMessage());
        } finally {
            try {
                if (out != null) out.close();
                if (fileStream != null) fileStream.close();
                if (conn != null) conn.disconnect();
            } catch (Exception ignored) {
            }
        }
        
        return result;
    }

    /**
     * V2 流式上传（小文件用）
     * 使用Chunked Streaming Mode，适合小于100MB的文件
     *
     * @param host      服务器地址
     * @param in        文件输入流
     * @param fileName  文件名
     * @param directory 存储目录
     * @param shareMode 共享模式
     * @param id        文件标识
     * @param token     认证令牌
     * @return 上传结果
     */
    public AttachResult uploadStreamV2(String host,
                                      InputStream in,
                                      String fileName,
                                      String directory,
                                      Integer shareMode,
                                      String id,
                                      String token) {
        AttachResult result = new AttachResult();
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(host + "/fileapi/api/v2/files/upload");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(8192);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            if (directory != null) {
                conn.setRequestProperty("X-Directory", directory);
            }
            if (id != null) {
                conn.setRequestProperty("X-File-Id", id);
            }
            if (shareMode != null) {
                conn.setRequestProperty("X-Share-Mode", shareMode.toString());
            }
            
            try (OutputStream out = conn.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream respIn = conn.getInputStream()) {
                    String resp = new String(respIn.readAllBytes());
                    JSONObject jo = JSONObject.parseObject(resp);
                    if (jo.getIntValue("code") == 200) {
                        result.setSuccess(true);
                        result.setCallbackData(jo.getJSONObject("data").getString("fileKey"));
                    } else {
                        result.setMessage(jo.getString("message"));
                    }
                }
            } else {
                result.setMessage("V2上传失败，HTTP状态码：" + responseCode);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("V2上传异常：" + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (conn != null) conn.disconnect();
            } catch (Exception ignored) {
            }
        }
        
        return result;
    }

    /**
     * 获取文件下载URL
     *
     * @param host    服务器地址
     * @param fileKey 文件key
     * @param token   认证令牌
     * @return 下载URL
     */
    public String getDownloadUrl(String host, String fileKey, String token) {
        return host + "/fileapi/api/v1/files/download?fileKey=" + fileKey + 
               (token != null ? "&token=" + token : "");
    }

    /**
     * 检查文件是否存在
     *
     * @param host    服务器地址
     * @param fileKey 文件key
     * @param token   认证令牌
     * @return 是否存在
     */
    public boolean checkFileExists(String host, String fileKey, String token) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(host + "/fileapi/api/v1/files/exists?fileKey=" + fileKey);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream in = conn.getInputStream()) {
                    String resp = new String(in.readAllBytes());
                    JSONObject jo = JSONObject.parseObject(resp);
                    return jo.getBooleanValue("exists");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }
}
