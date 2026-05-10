# 数据平台文件上传系统规格文档

## 1. 项目概述

### 1.1 项目背景
基于数据平台的文件上传系统，支持小文件流式上传和大文件分片上传，采用混合策略优化上传性能和可靠性。

### 1.2 核心特性
- **小文件（≤100MB）**：使用V2流式上传，直接传输文件流
- **大文件（>100MB）**：使用V1磁盘分片上传，支持断点续传
- **统一接口**：通过AttachBusiness接口适配不同存储后端
- **自动分流**：根据文件大小自动选择最优上传策略

## 2. 系统架构

### 2.1 技术栈
- **后端框架**：Spring Boot 2.7+
- **前端框架**：Vue 3 + Element Plus
- **HTTP客户端**：原生HttpURLConnection
- **构建工具**：Maven

### 2.2 核心组件

#### 2.2.1 AttachBusiness接口
定义所有存储后端的统一接口，包含以下方法：
- `upload(AttachParam)`：上传附件
- `download(AttachParam)`：下载附件
- `downloadAsStream(AttachParam)`：下载为输入流
- `checkConnection()`：检查连接

#### 2.2.2 DataPlatformFileClient
HTTP文件客户端，提供两个版本的API：
- **V1版本**：完整流上传（用于大文件合并后上传）
- **V2版本**：Chunked流式上传（用于小文件直接上传）

#### 2.2.3 DataPlatformMixBusiness
混合上传业务实现类，核心逻辑：
```
if (fileSize <= 100MB) {
    uploadStream() // V2流式上传
} else {
    uploadDisk()   // V1磁盘分片上传
}
```

## 3. 数据模型

### 3.1 AttachParam
上传参数模型，包含以下关键字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| fileCode | String | 文件唯一标识 |
| inputStream | InputStream | 文件输入流 |
| fileSize | String | 文件大小 |
| filePath | String | 存储路径 |
| chunkNo | String | 当前分片编号 |
| chunkQty | String | 总分片数 |
| name | String | 文件名 |

### 3.2 AttachResult
上传结果模型，包含：
| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 是否成功 |
| message | String | 结果消息 |
| callbackData | Object | 返回数据（fileKey） |

### 3.3 ObjectStorageModel
存储配置模型，包含：
| 字段 | 类型 | 说明 |
|------|------|------|
| hostAdr | String | 服务器地址 |
| accessKey | String | 认证令牌 |
| secretKey | String | 密钥 |
| bucketSht | String | 存储桶标识 |

## 4. API接口规范

### 4.1 文件上传接口

#### 4.1.1 V2流式上传（小文件）
```
POST /fileapi/api/v2/files/upload
Headers:
  Authorization: Bearer {token}
  Content-Type: application/octet-stream
  Content-Disposition: attachment;filename={fileName}
  X-Directory: {directory}
  X-File-Id: {fileId}
Body: [文件二进制流]
Response: { "code": 200, "data": { "fileKey": "xxx" } }
```

#### 4.1.2 V1分片上传（合并）
```
POST /fileapi/api/v1/files/upload
Headers:
  Authorization: Bearer {token}
  Content-Type: application/octet-stream
  X-Directory: {directory}
  X-File-Id: {fileId}
Body: [合并后的完整文件流]
Response: { "code": 200, "data": { "fileKey": "xxx" } }
```

### 4.2 应用层接口

#### 4.2.1 初始化上传
```
POST /api/upload/init
RequestBody: {
  "fileName": "example.pdf",
  "fileSize": "104857600",
  "fileMd5": "xxx",
  "chunkSize": "5242880"
}
Response: {
  "success": true,
  "data": {
    "fileCode": "xxx",
    "chunks": 20
  }
}
```

#### 4.2.2 分片上传
```
POST /api/upload/chunk?fileCode={fileCode}&chunkNum={chunkNum}&chunks={totalChunks}
RequestBody: [分片二进制数据]
Response: {
  "success": true,
  "message": "分片上传成功"
}
```

#### 4.2.3 合并分片
```
POST /api/upload/merge?fileCode={fileCode}
Response: {
  "success": true,
  "data": {
    "filePath": "xxx",
    "fileKey": "xxx"
  }
}
```

#### 4.2.4 查询上传状态
```
GET /api/upload/status/{fileCode}
Response: {
  "success": true,
  "data": {
    "fileCode": "xxx",
    "status": "uploading",
    "uploadedChunks": [1, 2, 3],
    "totalChunks": 20,
    "progress": 15
  }
}
```

## 5. 核心流程

### 5.1 小文件上传流程（V2）
1. 前端计算文件MD5
2. 调用初始化接口获取fileCode
3. 直接上传整个文件（使用V2 API）
4. 服务器返回fileKey
5. 更新上传状态

### 5.2 大文件分片上传流程（V1）
1. 前端计算文件MD5
2. 调用初始化接口获取fileCode
3. 前端将文件分片（每片5MB）
4. 并发上传所有分片到服务器
5. 服务器暂存分片到磁盘
6. 所有分片接收完成后，服务器合并分片
7. 调用V1 API上传合并后的完整文件
8. 返回fileKey，清理临时分片文件

## 6. 配置说明

### 6.1 应用配置
```yaml
upload:
  chunk-size: 5242880          # 分片大小 5MB
  max-file-size: 2147483648    # 最大文件 2GB
  temp-path: ./uploads/temp   # 分片临时目录
  max-stream-size: 104857600   # 小文件阈值 100MB
```

### 6.2 存储配置
通过ObjectStorageModel配置：
- `hostAdr`：数据平台服务器地址
- `accessKey`：认证令牌
- `bucketSht`：存储桶标识

## 7. 性能优化

### 7.1 小文件（≤100MB）
- 使用Chunked Streaming Mode，边读边传
- 无需暂存，直接上传
- 内存占用低

### 7.2 大文件（>100MB）
- 分片大小5MB，平衡网络和性能
- 支持并发上传多个分片
- 断点续传支持

### 7.3 并发控制
- 单文件最多3个分片并发上传
- 全局最多5个文件同时上传
- 单分片重试3次

## 8. 错误处理

### 8.1 网络错误
- 单分片上传失败自动重试
- 超时时间30秒
- 网络中断保存已上传状态

### 8.2 业务错误
- 文件大小超限：返回明确错误信息
- 文件类型不支持：前端拦截
- 上传失败：显示重试按钮

## 9. 验收标准

### 9.1 功能验收
- [x] 小文件（<100MB）使用V2流式上传
- [x] 大文件（≥100MB）使用V1分片上传
- [x] 自动根据文件大小选择上传策略
- [x] 支持断点续传
- [x] 上传进度实时展示
- [x] 暂停、继续、取消、重试功能

### 9.2 性能验收
- [x] 小文件上传速度不受分片影响
- [x] 大文件分片并发上传提高速度
- [x] 内存占用合理

### 9.3 稳定性验收
- [x] 网络中断后可恢复
- [x] 临时文件正确清理
- [x] 并发上传稳定可靠
