package com.upload.service;

import java.io.InputStream;

/**
 * 文件存储服务接口
 * 定义文件存储的基本操作
 *
 * @author system
 * @version 1.0.0
 */
public interface FileStorageService {
    
    /**
     * 保存分片文件
     *
     * @param fileCode   文件唯一编码
     * @param chunkNum   分片编号
     * @param inputStream 文件输入流
     * @return 是否保存成功
     */
    boolean saveChunk(String fileCode, int chunkNum, InputStream inputStream);
    
    /**
     * 获取分片文件
     *
     * @param fileCode 文件唯一编码
     * @param chunkNum 分片编号
     * @return 分片文件输入流
     */
    InputStream getChunk(String fileCode, int chunkNum);
    
    /**
     * 合并分片文件
     *
     * @param fileCode    文件唯一编码
     * @param totalChunks 总分片数
     * @param fileName    原始文件名
     * @return 合并后文件路径
     */
    String mergeChunks(String fileCode, int totalChunks, String fileName);
    
    /**
     * 删除分片文件
     *
     * @param fileCode 文件唯一编码
     * @return 是否删除成功
     */
    boolean deleteChunks(String fileCode);
    
    /**
     * 检查分片文件是否存在
     *
     * @param fileCode 文件唯一编码
     * @param chunkNum 分片编号
     * @return 是否存在
     */
    boolean chunkExists(String fileCode, int chunkNum);
    
    /**
     * 获取分片文件目录
     *
     * @param fileCode 文件唯一编码
     * @return 分片文件目录路径
     */
    String getChunkDirectory(String fileCode);
    
    /**
     * 清理过期分片文件
     *
     * @param maxAge 过期时间（毫秒）
     */
    void cleanupExpiredChunks(long maxAge);
}
