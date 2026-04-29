# SysRoleController API 接口文档

系统角色管理接口，提供角色的增删改查及权限分配功能。

## 基础信息

- **基地址**: `/system/role`
- **描述**: 系统角色管理相关功能
- **权限要求**: 需要 `role:super:admin` 或 `role:simple:admin` 角色

## 接口详情

### 1. 新增角色

- **接口路径**: `POST /system/role/add`
- **描述**: 新增系统角色
- **请求参数**:
  - Body: [SysRoleAddDTO](#sysroleadddto)

**请求示例**:
```json
{
  "roleName": "普通管理员",
  "roleKey": "simple_admin",
  "sort": 2
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

#### SysRoleAddDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| roleName | String | 是 | 角色名称 |
| roleKey | String | 是 | 角色标识 |
| sort | Integer | 否 | 显示排序 |

---

### 2. 更新角色信息

- **接口路径**: `POST /system/role/update`
- **描述**: 更新角色信息
- **请求参数**:
  - Body: [SysRoleUpdateDTO](#sysroleupdatedto)

**请求示例**:
```json
{
  "id": 1,
  "roleName": "超级管理员更新",
  "roleKey": "super_admin",
  "sort": 1
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

#### SysRoleUpdateDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | BigInteger | 是 | 角色ID |
| roleName | String | 是 | 角色名称 |
| roleKey | String | 是 | 角色标识 |
| sort | Integer | 否 | 显示排序 |

---

### 3. 删除角色

- **接口路径**: `POST /system/role/delete/{id}`
- **描述**: 删除单个角色
- **请求参数**:
  - id (BigInteger): 角色ID（路径参数）

**请求示例**:
```
POST /system/role/delete/1
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

### 4. 批量删除角色

- **接口路径**: `POST /system/role/deleteBatch`
- **描述**: 批量删除角色
- **请求参数**:
  - Body: List<BigInteger> 角色ID集合

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

### 5. 给角色赋予权限

- **接口路径**: `POST /system/role/grantPermissionsByRoleId`
- **描述**: 为角色分配菜单权限
- **请求参数**:
  - Body: List<[SysRoleMenuVO](#sysrolemenervo)>

**请求示例**:
```json
[
  {
    "roleId": 1,
    "menuId": 1
  },
  {
    "roleId": 1,
    "menuId": 2
  },
  {
    "roleId": 1,
    "menuId": 3
  }
]
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

#### SysRoleMenuVO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| roleId | BigInteger | 是 | 角色ID |
| menuId | BigInteger | 是 | 菜单ID |

---

### 6. 分页查询角色

- **接口路径**: `POST /system/role/listByPage`
- **描述**: 分页查询角色列表
- **请求参数**:
  - Body: [SysRolePageDTO](#sysrolepagedto)

**请求示例**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sysRole": {
    "roleName": "管理员",
    "roleKey": "admin",
    "remark": "系统管理员"
  }
}
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([ApiPage](#apipage)\<[SysRoleVO](#sysrolevo)\>): 分页角色数据
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "rows": [
      {
        "id": "1",
        "roleName": "超级管理员",
        "roleKey": "super_admin",
        "remark": "系统超级管理员",
        "sort": 1,
        "createBy": 1,
        "createTime": "2024-01-01T12:00:00",
        "updateBy": null,
        "updateTime": null
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 10
  },
  "message": "success"
}
```

#### SysRolePageDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| sysRole | Query | 是 | 查询条件 |

##### Query 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| roleName | String | 否 | 角色名称 |
| roleKey | String | 否 | 角色key值 |
| remark | String | 否 | 备注 |

#### ApiPage 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| rows | List\<T\> | 数据列表 |
| total | Long | 总记录数 |
| pageNum | Integer | 当前页码 |
| pageSize | Integer | 每页条数 |

#### SysRoleVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | String | 角色ID |
| roleName | String | 角色名称 |
| roleKey | String | 角色键值 |
| remark | String | 备注信息 |
| sort | Integer | 排序号 |
| createBy | BigInteger | 创建人 |
| createTime | LocalDateTime | 创建时间 |
| updateBy | BigInteger | 更新人 |
| updateTime | LocalDateTime | 更新时间 |