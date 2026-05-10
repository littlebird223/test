package com.upload.repository;

import com.upload.model.UploadTask;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 上传任务仓库实现类
 * 使用内存HashMap存储上传任务信息
 *
 * @author system
 * @version 1.0.0
 */
@Repository
public class UploadTaskRepositoryImpl implements UploadTaskRepository {
    
    private final Map<String, UploadTask> taskMap = new ConcurrentHashMap<>();
    
    private final Map<String, UploadTask> fileMd5Index = new ConcurrentHashMap<>();
    
    @Override
    public UploadTask save(UploadTask task) {
        if (task == null || task.getFileCode() == null) {
            throw new IllegalArgumentException("任务或文件编码不能为空");
        }
        
        taskMap.put(task.getFileCode(), task);
        
        if (task.getFileMd5() != null) {
            fileMd5Index.put(task.getFileMd5(), task);
        }
        
        return task;
    }
    
    @Override
    public UploadTask findByFileCode(String fileCode) {
        if (fileCode == null) {
            return null;
        }
        return taskMap.get(fileCode);
    }
    
    @Override
    public UploadTask update(UploadTask task) {
        if (task == null || task.getFileCode() == null) {
            throw new IllegalArgumentException("任务或文件编码不能为空");
        }
        
        if (!taskMap.containsKey(task.getFileCode())) {
            throw new IllegalStateException("任务不存在: " + task.getFileCode());
        }
        
        taskMap.put(task.getFileCode(), task);
        return task;
    }
    
    @Override
    public boolean deleteByFileCode(String fileCode) {
        if (fileCode == null) {
            return false;
        }
        
        UploadTask task = taskMap.remove(fileCode);
        
        if (task != null && task.getFileMd5() != null) {
            fileMd5Index.remove(task.getFileMd5());
        }
        
        return task != null;
    }
    
    @Override
    public List<UploadTask> findAll() {
        return new ArrayList<>(taskMap.values());
    }
    
    @Override
    public List<UploadTask> findByStatus(String status) {
        return taskMap.values().stream()
                .filter(task -> status.equals(task.getStatus()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByFileCode(String fileCode) {
        return fileCode != null && taskMap.containsKey(fileCode);
    }
    
    /**
     * 根据文件MD5查询任务
     *
     * @param fileMd5 文件MD5值
     * @return 上传任务对象
     */
    public UploadTask findByFileMd5(String fileMd5) {
        return fileMd5Index.get(fileMd5);
    }
    
    /**
     * 根据业务标识查询任务
     *
     * @param tableName 业务表名
     * @param pgmId     程序ID
     * @return 上传任务列表
     */
    public List<UploadTask> findByBusinessKey(String tableName, String pgmId) {
        return taskMap.values().stream()
                .filter(task -> Objects.equals(tableName, task.getTableName()))
                .filter(task -> Objects.equals(pgmId, task.getPgmId()))
                .collect(Collectors.toList());
    }
}
