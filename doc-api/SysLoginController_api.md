# SysLoginController API 接口文档

系统登录相关接口，提供用户登录、退出、验证码等功能。

## 基础信息

- **基地址**: `/system`
- **描述**: 系统登录认证相关功能

## 接口详情

### 1. 后台系统用户登录

- **接口路径**: `POST /system/login`
- **描述**: 用户登录接口，使用SM2加密密码
- **请求参数**:
  - Body: [SysLoginDTO](#syslogindto)

**请求示例**:
```json
{
  "username": "admin",
  "password": "encrypted_password_here",
  "captcha": "ABCD",
  "captchaId": "UUID-STRING",
  "publicKey": "PUBLIC_KEY_HEX",
  "rememberMe": true
}
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (String): 登录成功后的token
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "success"
}
```

#### SysLoginDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| username | String | 是 | 用户账号 |
| password | String | 是 | 用户密码（SM2加密后） |
| captcha | String | 是 | 登录验证码 |
| captchaId | String | 是 | 验证码ID |
| publicKey | String | 是 | SM2公钥 |
| rememberMe | Boolean | 否 | 是否记住我 |

---

### 2. 后台系统用户退出登录

- **接口路径**: `POST /system/logout`
- **描述**: 用户退出登录
- **请求参数**: 无（需要携带token）
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (Object): 响应数据
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "message": "success"
}
```

---

### 3. 获取用户登录信息

- **接口路径**: `GET /system/loginInfo`
- **描述**: 获取当前登录用户的详细信息
- **请求参数**: 无（需要携带token）
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([SysLoginUserVO](#sysloginuservo)): 用户登录信息
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "loginStatus": true,
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "avatar": "/avatar/admin.png",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "success"
}
```

#### SysLoginUserVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| loginStatus | Boolean | 登录状态 |
| id | BigInteger | 用户ID |
| username | String | 用户名 |
| nickname | String | 昵称 |
| avatar | String | 头像URL |
| token | String | 认证令牌 |

---

### 4. 获取用户菜单

- **接口路径**: `GET /system/user/menus`
- **描述**: 获取当前用户的菜单权限树
- **请求参数**: 无（需要携带token）
- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (List<Tree\<BigInteger\>>): 菜单树形结构
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "系统管理",
      "path": "/system",
      "children": [
        {
          "id": 2,
          "name": "用户管理",
          "path": "/system/user"
        },
        {
          "id": 3,
          "name": "角色管理",
          "path": "/system/role"
        }
      ]
    }
  ],
  "message": "success"
}
```

---

### 5. 获取后台登录验证码

- **接口路径**: `GET /system/captcha`
- **描述**: 获取登录验证码图片及SM2公钥
- **请求参数**:
  - timestamp (String): 时间戳（可选）

**请求示例**:
```
GET /system/captcha?timestamp=1704067200000
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([SysCaptchaVO](#syscaptchavo)): 验证码信息
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "captchaId": "UUID-STRING",
    "imgBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "publicKey": "PUBLIC_KEY_HEX_STRING"
  },
  "message": "success"
}
```

#### SysCaptchaVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| captchaId | String | 验证码ID |
| imgBase64 | String | 验证码base64字符串 |
| publicKey | String | SM2公钥（用于加密密码） |

**说明**:
1. 调用此接口获取验证码和公钥
2. 前端使用公钥对用户密码进行SM2加密
3. 登录时携带验证码ID、验证码内容和加密后的密码
4. 验证码有效期为300秒（5分钟）