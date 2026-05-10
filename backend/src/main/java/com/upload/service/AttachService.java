package com.upload.service;

import com.upload.model.AttachParam;
import com.upload.model.AttachResult;
import com.upload.model.ServiceParams;
import com.upload.model.UploadTask;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 * 定义文件上传的核心业务逻辑
 *
 * @author system
 * @version 1.0.0
 */
public interface AttachService {
    
    /**
     * 初始化上传任务
     *
     * @param serviceParams 前端参数
     * @return 上传结果
     */
    AttachResult initUpload(ServiceParams serviceParams);
    
    /**
     * 上传分片文件
     *
     * @param serviceParams 前端参数
     * @param file          分片文件
     * @return 上传结果
     */
    AttachResult uploadChunk(ServiceParams serviceParams, MultipartFile file);
    
    /**
     * 合并分片文件
     *
     * @param fileCode 文件编码
     * @return 合并结果
     */
    AttachResult mergeChunks(String fileCode);
    
    /**
     * 查询上传状态
     *
     * @param fileCode 文件编码
     * @return 上传任务信息
     */
    UploadTask getUploadStatus(String fileCode);
    
    /**
     * 取消上传任务
     *
     * @param fileCode 文件编码
     * @return 是否取消成功
     */
    boolean cancelUpload(String fileCode);
    
    /**
     * 删除已上传文件
     *
     * @param fileCode 文件编码
     * @return 是否删除成功
     */
    boolean deleteUpload(String fileCode);
}
