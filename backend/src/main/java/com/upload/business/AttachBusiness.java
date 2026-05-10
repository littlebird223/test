package com.upload.business;

import com.upload.model.AttachParam;
import com.upload.model.AttachResult;

import java.io.InputStream;

/**
 * 附件业务接口
 * 定义不同存储方式（阿里云OSS、华为云OBS、数据平台等）的统一接口
 * 
 * @author system
 * @version 1.0.0
 */
public interface AttachBusiness {
    
    /**
     * 上传附件
     *
     * @param param 上传参数
     * @return 上传结果
     */
    AttachResult upload(AttachParam param);
    
    /**
     * 下载附件到指定输出流
     *
     * @param param 下载参数
     * @return 是否下载成功
     */
    boolean download(AttachParam param);
    
    /**
     * 下载附件为输入流
     *
     * @param param 下载参数
     * @return 文件输入流
     */
    InputStream downloadAsStream(AttachParam param);
    
    /**
     * 检查连接
     *
     * @return 是否连接正常
     */
    boolean checkConnection();
}
