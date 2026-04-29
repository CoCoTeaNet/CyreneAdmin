# SysLogController API 接口文档

系统用户访问日志接口，提供日志查询和删除功能。

## 基础信息

- **基地址**: `/system/log`
- **描述**: 系统用户访问日志管理相关功能
- **权限要求**: 需要 `role:super:admin` 或 `role:simple:admin` 角色

## 接口详情

### 1. 列表分页查询

- **接口路径**: `POST /system/log/listByPage`
- **描述**: 分页查询系统日志列表
- **请求参数**:
  - Body: [SysLogPageDTO](#syslogpagedto)

**请求示例**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sysLog": {
    "id": "1",
    "operator": "admin",
    "requestWay": "GET",
    "apiPath": "/system/user/listByPage"
  }
}
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([ApiPage](#apipage)\<[SysLogVO](#syslogvo)\>): 分页日志数据
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "rows": [
      {
        "id": "1",
        "ipAddress": "127.0.0.1",
        "operator": "admin",
        "username": "admin",
        "nickname": "管理员",
        "requestWay": "GET",
        "apiPath": "/system/user/listByPage",
        "logStatus": 1,
        "logType": 1,
        "createTime": "2024-01-01T12:00:00"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  },
  "message": "success"
}
```

#### SysLogPageDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| sysLog | Query | 是 | 查询条件 |

##### Query 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | String | 否 | 日志编号 |
| operator | String | 否 | 操作人 |
| requestWay | String | 否 | 请求方法 |
| apiPath | String | 否 | 接口路径 |

#### ApiPage 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| rows | List\<T\> | 数据列表 |
| total | Long | 总记录数 |
| pageNum | Integer | 当前页码 |
| pageSize | Integer | 每页条数 |

#### SysLogVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | String | 日志编号 |
| ipAddress | String | IP地址 |
| operator | String | 操作人 |
| username | String | 操作人名称 |
| nickname | String | 操作人昵称 |
| requestWay | String | 请求方法 |
| apiPath | String | 接口路径 |
| logStatus | Integer | 日志状态 |
| logType | Integer | 日志类型 |
| createTime | LocalDateTime | 操作时间 |

---

### 2. 批量删除日志

- **接口路径**: `POST /system/log/deleteBatch`
- **描述**: 批量删除系统日志
- **请求参数**:
  - Body: List<BigInteger> 日志ID集合

**请求示例**:
```json
[1, 2, 3]
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (Boolean): 操作结果，成功返回 true
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": true,
  "message": "success"
}
```