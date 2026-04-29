# SysDashboardController API 接口文档

系统仪表盘接口，提供系统概览和服务器信息等功能。

## 基础信息

- **基地址**: `/system/dashboard`
- **描述**: 提供系统仪表盘相关功能

## 接口详情

### 1. 测试服务是否运行成功

- **接口路径**: `GET /system/dashboard/index`
- **描述**: 测试服务是否运行成功
- **请求参数**: 无
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (String): 响应数据，固定返回 "Hello world"
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": "Hello world",
  "message": "success"
}
```

### 2. 获取系统数据概览

- **接口路径**: `GET /system/dashboard/getCount`
- **描述**: 获取系统数据概览
- **请求参数**: 无
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (List<[SysOverviewVO](#sysoverviewvo)>): 系统概览数据列表
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "title": "用户总数",
      "count": 100
    },
    {
      "title": "活跃用户数",
      "count": 80
    }
  ],
  "message": "success"
}
```

#### SysOverviewVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| title | String | 标题 |
| count | Long | 数量 |

### 3. 获取服务器运行信息

- **接口路径**: `GET /system/dashboard/getSystemInfo`
- **描述**: 获取服务器运行信息
- **请求参数**: 无
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([SystemInfoVO](#systeminfovo)): 服务器运行信息
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "os": "Windows 10",
    "serverName": "localhost",
    "serverIp": "127.0.0.1",
    "serverArchitecture": "amd64",
    "javaName": "OpenJDK",
    "javaVersion": "1.8.0_301",
    "javaPath": "/usr/lib/jvm/java-8-openjdk-amd64",
    "projectPath": "/app/cyrene-admin",
    "runningTime": 3600000,
    "cpuSystemUsed": 10.5,
    "cpuUserUsed": 20.3,
    "cpuFree": 69.2,
    "cpuCount": 4,
    "memoryTotalSize": 8589934592,
    "memoryAvailableSize": 4294967296,
    "diskTotalSize": 536870912000,
    "diskFreeSize": 268435456000,
    "diskPath": "/",
    "diskSeparator": "/"
  },
  "message": "success"
}
```

#### SystemInfoVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| os | String | 操作系统 |
| serverName | String | 服务名称 |
| serverIp | String | 服务IP |
| serverArchitecture | String | 服务器架构 |
| javaName | String | JAVA名称 |
| javaVersion | String | JAVA版本 |
| javaPath | String | JAVA存放路径 |
| projectPath | String | 项目路径 |
| runningTime | Long | 服务运行时长 |
| cpuSystemUsed | Double | CPU系统使用率 |
| cpuUserUsed | Double | CPU用户使用率 |
| cpuFree | Double | CPU可用率 |
| cpuCount | Integer | CPU数量 |
| memoryTotalSize | Long | 内存总大小 |
| memoryAvailableSize | Long | 内存可用大小 |
| diskTotalSize | Long | 磁盘总大小 |
| diskFreeSize | Long | 磁盘可用大小 |
| diskPath | String | 盘符路径 |
| diskSeparator | String | 磁盘分隔符 |