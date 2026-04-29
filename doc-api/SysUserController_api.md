# SysUserController API 接口文档

系统用户管理接口，提供用户的增删改查、个人信息管理、密码修改和头像上传等功能。

## 基础信息

- **基地址**: `/system/user`
- **描述**: 系统用户管理相关功能
- **权限要求**: 大部分接口需要 `role:super:admin` 或 `role:simple:admin` 角色

## 接口详情

### 1. 新增用户

- **接口路径**: `POST /system/user/add`
- **描述**: 新增系统用户
- **请求参数**:
  - Body: [SysUserAddDTO](#sysuseradddto)

**请求示例**:
```json
{
  "username": "testuser",
  "nickname": "测试用户",
  "sex": 1,
  "email": "test@example.com",
  "password": "123456",
  "accountStatus": 1,
  "roleIds": [1, 2]
}
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

#### SysUserAddDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| username | String | 是 | 用户名称（账号） |
| nickname | String | 否 | 用户昵称 |
| sex | String | 否 | 用户性别 |
| email | String | 否 | 用户邮箱 |
| password | String | 否 | 用户密码 |
| accountStatus | String | 否 | 账号状态 |
| roleIds | List\<BigInteger\> | 是 | 用户角色ID列表 |

---

### 2. 更新用户信息

- **接口路径**: `POST /system/user/update`
- **描述**: 更新用户信息（管理员操作）
- **请求参数**:
  - Body: [SysUserUpdateDTO](#sysuserupdatedto)

**请求示例**:
```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员更新",
  "sex": 1,
  "email": "admin@example.com",
  "mobilePhone": "13800138000",
  "accountStatus": 1,
  "roleIds": [1, 2]
}
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

#### SysUserUpdateDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | BigInteger | 是 | 用户ID |
| username | String | 是 | 用户名称 |
| nickname | String | 否 | 用户昵称 |
| sex | String | 否 | 用户性别 |
| email | String | 否 | 用户邮箱 |
| mobilePhone | String | 否 | 用户手机号 |
| accountStatus | String | 否 | 账号状态 |
| roleIds | List\<BigInteger\> | 否 | 用户角色ID列表 |

---

### 3. 删除用户

- **接口路径**: `POST /system/user/delete/{id}`
- **描述**: 删除单个用户
- **请求参数**:
  - id (BigInteger): 用户ID（路径参数）

**请求示例**:
```
POST /system/user/delete/1
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

---

### 4. 批量删除用户

- **接口路径**: `POST /system/user/deleteBatch`
- **描述**: 批量删除用户
- **请求参数**:
  - Body: List<BigInteger> 用户ID集合

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

---

### 5. 分页查询用户

- **接口路径**: `POST /system/user/listByPage`
- **描述**: 分页查询用户列表
- **请求参数**:
  - Body: [SysUserPageDTO](#sysuserpagedto)

**请求示例**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sysUser": {
    "nickname": "管理员",
    "username": "admin",
    "sex": 1,
    "mobilePhone": "13800138000",
    "email": "admin@example.com",
    "accountStatus": 1
  }
}
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([ApiPage](#apipage)\<[SysUserVO](#sysuservo)\>): 分页用户数据
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "rows": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "sex": 1,
        "email": "admin@example.com",
        "mobilePhone": "13800138000",
        "accountStatus": 1,
        "avatar": "/avatar/admin.png",
        "lastLoginIp": "127.0.0.1",
        "lastLoginTime": "2024-01-01T12:00:00",
        "createBy": 1,
        "createTime": "2024-01-01T12:00:00",
        "updateBy": null,
        "updateTime": null,
        "roleList": [
          {
            "roleId": 1,
            "roleName": "超级管理员"
          }
        ]
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  },
  "message": "success"
}
```

#### SysUserPageDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| sysUser | Query | 是 | 查询条件 |

##### Query 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| nickname | String | 否 | 用户昵称 |
| username | String | 否 | 用户账号 |
| sex | Integer | 否 | 用户性别 |
| mobilePhone | String | 否 | 用户手机号 |
| email | String | 否 | 用户邮箱 |
| accountStatus | Integer | 否 | 账号状态 |

#### ApiPage 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| rows | List\<T\> | 数据列表 |
| total | Long | 总记录数 |
| pageNum | Integer | 当前页码 |
| pageSize | Integer | 每页条数 |

#### SysUserVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | BigInteger | 用户ID |
| username | String | 账户名 |
| nickname | String | 用户昵称 |
| sex | Integer | 用户性别 |
| email | String | 邮箱地址 |
| mobilePhone | String | 用户手机号 |
| accountStatus | Integer | 账号状态 |
| avatar | String | 头像地址 |
| lastLoginIp | String | 最后登录IP |
| lastLoginTime | LocalDateTime | 最后登录时间 |
| createBy | BigInteger | 创建人 |
| createTime | LocalDateTime | 创建时间 |
| updateBy | BigInteger | 更新人 |
| updateTime | LocalDateTime | 更新时间 |
| roleList | List\<[SysUserRoleVO](#sysuserrolevo)\> | 角色列表 |

##### SysUserRoleVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| roleId | BigInteger | 角色ID |
| roleName | String | 角色名称 |

---

### 6. 获取用户详细信息

- **接口路径**: `GET /system/user/getDetail`
- **描述**: 获取当前登录用户的详细信息
- **请求参数**: 无（需要携带token）
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([SysUserVO](#sysuservo)): 用户详细信息
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "sex": 1,
    "email": "admin@example.com",
    "mobilePhone": "13800138000",
    "accountStatus": 1,
    "avatar": "/avatar/admin.png",
    "lastLoginIp": "127.0.0.1",
    "lastLoginTime": "2024-01-01T12:00:00",
    "createBy": 1,
    "createTime": "2024-01-01T12:00:00",
    "updateBy": null,
    "updateTime": null,
    "roleList": [
      {
        "roleId": 1,
        "roleName": "超级管理员"
      }
    ]
  },
  "message": "success"
}
```

---

### 7. 更新登录用户信息

- **接口路径**: `POST /system/user/updateByUser`
- **描述**: 当前登录用户更新自己的基本信息
- **请求参数**:
  - Body: [SysLoginUserUpdateDTO](#sysloginuserupdatedto)

**请求示例**:
```json
{
  "nickname": "新昵称",
  "sex": 1,
  "email": "newemail@example.com",
  "mobilePhone": "13800138000"
}
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

#### SysLoginUserUpdateDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| nickname | String | 否 | 用户昵称 |
| sex | String | 否 | 用户性别 |
| email | String | 否 | 用户邮箱 |
| mobilePhone | String | 否 | 用户手机号 |

---

### 8. 修改用户密码

- **接口路径**: `POST /system/user/doModifyPassword`
- **描述**: 修改当前登录用户的密码
- **请求参数**:
  - Body: JSONObject

**请求示例**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
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

---

### 9. 获取用户头像

- **接口路径**: `GET /system/user/getAvatar`
- **描述**: 获取用户头像文件
- **请求参数**:
  - avatar (String): 头像文件名称（查询参数）

**请求示例**:
```
GET /system/user/getAvatar?avatar=avatar_name.png
```

- **响应**: 直接返回图片文件流

---

### 10. 上传用户头像

- **接口路径**: `POST /system/user/avatar/upload`
- **描述**: 上传用户头像
- **请求参数**:
  - file (UploadedFile): 头像文件（表单参数）

**请求示例**:
```
POST /system/user/avatar/upload
Content-Type: multipart/form-data

file: [binary image data]
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

**说明**:
- 支持的文件类型由系统配置决定
- 上传后会自动验证头像文件的合法性
- 上传成功后会自动更新当前用户的头像信息