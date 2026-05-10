# 分片文件上传服务规格文档

## 1. 背景与目标

### 为什么需要这个系统
当前的文件上传方案存在以下问题：
- 大文件上传失败率高，网络中断需要重新上传整个文件
- 无法支持断点续传，用户体验差
- 并发上传效率低，服务器压力大

通过实现基于分片的文件上传系统，可以：
- 支持大文件分片上传，提高上传成功率
- 实现断点续传功能，网络中断后可继续上传
- 支持并发上传多个分片，提高上传速度
- 提供完善的上传进度展示

### 目标
构建一个企业级的分片文件上传服务，支持：
- 大文件分片上传
- 断点续传
- 上传进度实时展示
- 支持阿里云OSS和华为云OBS等多种对象存储

## 2. 系统架构

### 技术栈
- **后端**: Spring Boot 2.7+
- **前端**: Vue 3 + Element Plus
- **存储**: 模拟本地存储（可扩展为阿里云OSS、华为云OBS）
- **构建工具**: Maven

### 核心功能模块

#### 2.1 后端模块
1. **AttachController** - 文件上传控制器
   - `POST /api/upload/chunk` - 分片文件上传接口
   - `POST /api/upload/init` - 初始化上传（生成fileCode）
   - `POST /api/upload/merge` - 合并分片文件
   - `GET /api/upload/status/{fileCode}` - 查询上传状态

2. **AttachService** - 文件上传服务
   - 分片文件接收和存储
   - 分片合并逻辑
   - 上传状态管理
   - MD5校验

3. **FileStorageService** - 文件存储服务
   - 本地文件系统存储
   - 存储路径管理
   - 文件清理机制

#### 2.2 前端模块
1. **FileUploader组件** - 核心上传组件
   - 文件选择和拖拽上传
   - 分片策略（每片5MB）
   - 并发上传控制
   - 进度展示
   - 断点续传支持

2. **UploadList组件** - 上传列表组件
   - 上传队列管理
   - 状态展示（上传中、暂停、完成、失败）
   - 操作控制（暂停、继续、取消、重试）

## 3. API接口规范

### 3.1 初始化上传
**请求**
```http
POST /api/upload/init
Content-Type: application/json

{
  "fileName": "example.pdf",
  "fileSize": 104857600,
  "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
  "chunkSize": 5242880,
  "tableName": "document",
  "pgmId": "doc_001"
}
```

**响应**
```json
{
  "success": true,
  "data": {
    "fileCode": "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
    "chunks": 20,
    "uploadedChunks": []
  }
}
```

### 3.2 分片上传
**请求**
```http
POST /api/upload/chunk?fileCode={fileCode}&chunkNum={chunkNum}&chunks={totalChunks}
Content-Type: multipart/form-data

file: [二进制数据]
```

**响应**
```json
{
  "success": true,
  "message": "分片上传成功",
  "data": {
    "chunkNum": 1,
    "chunkSize": 5242880
  }
}
```

### 3.3 合并分片
**请求**
```http
POST /api/upload/merge?fileCode={fileCode}
```

**响应**
```json
{
  "success": true,
  "data": {
    "filePath": "/uploads/2024/01/example.pdf",
    "fileSize": 104857600
  }
}
```

### 3.4 查询上传状态
**请求**
```http
GET /api/upload/status/{fileCode}
```

**响应**
```json
{
  "success": true,
  "data": {
    "fileCode": "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
    "status": "uploading",
    "uploadedChunks": [1, 2, 3, 5],
    "totalChunks": 20,
    "progress": 25
  }
}
```

## 4. 数据模型

### 4.1 上传任务表（UploadTask）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| file_code | VARCHAR(64) | 文件唯一标识 |
| file_name | VARCHAR(255) | 原始文件名 |
| file_size | BIGINT | 文件大小 |
| file_md5 | VARCHAR(64) | 文件MD5 |
| chunk_size | INT | 分片大小 |
| total_chunks | INT | 总分片数 |
| uploaded_chunks | TEXT | 已上传分片列表(JSON) |
| status | VARCHAR(20) | 状态（pending/uploading/completed/failed） |
| file_path | VARCHAR(500) | 合并后文件路径 |
| table_name | VARCHAR(100) | 业务表名 |
| pgm_id | VARCHAR(100) | 程序ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.2 分片文件存储
分片文件存储结构：
```
/uploads/temp/{fileCode}/
  ├── {fileCode}_0
  ├── {fileCode}_1
  ├── ...
  └── {fileCode}_{n}
```

合并后文件存储结构：
```
/uploads/files/{year}/{month}/
  └── {fileCode}_{originalName}
```

## 5. 前端界面设计

### 5.1 页面布局
采用现代化的卡片式布局：
- 左侧：上传区域（拖拽+点击上传）
- 右侧：上传队列列表
- 底部：统计信息栏

### 5.2 核心交互流程
1. **选择文件**
   - 拖拽文件到上传区域
   - 或点击选择文件按钮
   - 支持多文件选择

2. **开始上传**
   - 自动计算分片数量
   - 调用初始化接口获取fileCode
   - 并发上传所有分片（最多3个并发）
   - 实时更新进度条

3. **上传完成**
   - 所有分片上传完成后自动调用合并接口
   - 展示合并状态
   - 合并成功后显示文件信息

4. **断点续传**
   - 上传中断后保存已上传分片信息
   - 刷新页面后自动查询已上传分片
   - 从断点处继续上传

### 5.3 视觉效果
- 主题色：#409EFF（Element Plus蓝）
- 进度条：渐变色 (#409EFF → #67C23A)
- 卡片阴影：0 2px 12px rgba(0, 0, 0, 0.1)
- 动画：所有状态变化使用过渡动画

## 6. 性能要求

### 6.1 并发控制
- 单个文件最多3个分片并发上传
- 全局最多5个文件同时上传
- 分片大小建议5MB（可配置）

### 6.2 错误处理
- 单个分片失败自动重试3次
- 网络超时时间30秒
- 上传中断后自动保存状态

### 6.3 资源清理
- 24小时内未完成的分片文件自动清理
- 合并完成后立即删除分片文件
- 临时文件夹定期清理

## 7. 安全考虑

### 7.1 文件验证
- 文件类型白名单校验
- 文件大小限制（最大2GB）
- MD5完整性校验

### 7.2 路径安全
- 禁止路径穿越（../）
- 文件名特殊字符过滤
- 存储路径与业务隔离

## 8. 影响范围

### 8.1 涉及的系统
- 文件上传服务（新增）
- 前端文件上传组件（新增）

### 8.2 依赖关系
- 后端依赖Spring Boot框架
- 前端依赖Vue 3和Element Plus
- 使用本地文件系统作为默认存储

## 9. 验收标准

### 9.1 功能验收
- [ ] 可以上传小于分片大小的文件（不分片）
- [ ] 可以上传大于分片大小的文件（自动分片）
- [ ] 可以并发上传多个分片
- [ ] 可以查看上传进度
- [ ] 可以暂停和继续上传
- [ ] 可以取消上传
- [ ] 上传失败可以重试
- [ ] 页面刷新后可以断点续传
- [ ] 分片合并后文件完整可用

### 9.2 性能验收
- [ ] 10MB文件上传时间 < 5秒
- [ ] 100MB文件上传时间 < 30秒
- [ ] 支持100个并发上传请求

### 9.3 稳定性验收
- [ ] 网络中断后可以恢复上传
- [ ] 服务器重启后分片文件不丢失
- [ ] 临时文件按时清理
