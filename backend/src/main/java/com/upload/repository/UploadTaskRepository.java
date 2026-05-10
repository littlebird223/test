package com.upload.repository;

import com.upload.model.UploadTask;

import java.util.List;

/**
 * 上传任务数据仓库接口
 * 定义上传任务的基本CRUD操作
 *
 * @author system
 * @version 1.0.0
 */
public interface UploadTaskRepository {
    
    /**
     * 保存上传任务
     *
     * @param task 上传任务对象
     * @return 保存后的任务对象
     */
    UploadTask save(UploadTask task);
    
    /**
     * 根据文件编码查询任务
     *
     * @param fileCode 文件唯一编码
     * @return 上传任务对象，如果不存在返回null
     */
    UploadTask findByFileCode(String fileCode);
    
    /**
     * 更新上传任务
     *
     * @param task 上传任务对象
     * @return 更新后的任务对象
     */
    UploadTask update(UploadTask task);
    
    /**
     * 根据文件编码删除任务
     *
     * @param fileCode 文件唯一编码
     * @return 是否删除成功
     */
    boolean deleteByFileCode(String fileCode);
    
    /**
     * 查询所有上传任务
     *
     * @return 所有上传任务列表
     */
    List<UploadTask> findAll();
    
    /**
     * 根据状态查询上传任务
     *
     * @param status 任务状态
     * @return 符合状态的上传任务列表
     */
    List<UploadTask> findByStatus(String status);
    
    /**
     * 检查任务是否存在
     *
     * @param fileCode 文件唯一编码
     * @return 是否存在
     */
    boolean existsByFileCode(String fileCode);
}
