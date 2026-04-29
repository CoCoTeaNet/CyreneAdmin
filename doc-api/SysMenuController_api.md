# SysMenuController API 接口文档

系统菜单或权限管理接口，提供菜单和权限的增删改查及树形结构查询功能。

## 基础信息

- **基地址**: `/system/menu`
- **描述**: 系统菜单和权限管理相关功能
- **权限要求**: 需要 `role:super:admin` 或 `role:simple:admin` 角色

## 接口详情

### 1. 新增菜单或权限

- **接口路径**: `POST /system/menu/add`
- **描述**: 新增菜单或权限项
- **请求参数**:
  - Body: [SysMenuAddDTO](#sysmenuadddto)

**请求示例**:
```json
{
  "menuName": "用户管理",
  "permissionCode": "system:user:list",
  "routerPath": "/system/user",
  "parentId": "1",
  "menuType": 1,
  "isMenu": 1,
  "menuStatus": 1,
  "sort": 1,
  "componentPath": "system/user/index",
  "isExternalLink": 0,
  "iconPath": "user"
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

#### SysMenuAddDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| menuName | String | 是 | 菜单名称 |
| permissionCode | String | 否 | 权限编号 |
| routerPath | String | 否 | 路由地址 |
| parentId | String | 否 | 父主键ID |
| menuType | String | 否 | 按钮类型;0目录 1菜单 2按钮 |
| isMenu | String | 是 | 是否菜单 |
| menuStatus | String | 否 | 菜单状态 |
| sort | Integer | 否 | 显示顺序 |
| componentPath | String | 否 | 组件路径 |
| isExternalLink | String | 否 | 是否外链;0是 1否 |
| iconPath | String | 否 | 菜单图标 |

---

### 2. 批量删除菜单或权限

- **接口路径**: `POST /system/menu/deleteBatch`
- **描述**: 批量删除菜单或权限
- **请求参数**:
  - Body: List<BigInteger> ID集合

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

### 3. 更新菜单或权限信息

- **接口路径**: `POST /system/menu/update`
- **描述**: 更新菜单或权限信息
- **请求参数**:
  - Body: [SysMenuUpdateDTO](#sysmenuupdatedto)

**请求示例**:
```json
{
  "id": 1,
  "menuName": "用户管理更新",
  "permissionCode": "system:user:list",
  "routerPath": "/system/user",
  "parentId": "1",
  "menuType": 1,
  "isMenu": 1,
  "menuStatus": 1,
  "sort": 2,
  "componentPath": "system/user/index",
  "isExternalLink": 0,
  "iconPath": "user"
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

#### SysMenuUpdateDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | BigInteger | 是 | 菜单ID |
| menuName | String | 是 | 菜单名称 |
| permissionCode | String | 否 | 权限编号 |
| routerPath | String | 否 | 路由路径 |
| parentId | String | 否 | 父菜单ID |
| menuType | String | 否 | 菜单类型 |
| isMenu | String | 是 | 是否菜单 |
| menuStatus | String | 否 | 菜单状态 |
| sort | Integer | 否 | 序号 |
| componentPath | String | 否 | 组件路径 |
| isExternalLink | String | 否 | 是否拓展链接 |
| iconPath | String | 否 | 图标路径 |

---

### 4. 分页查询菜单或权限

- **接口路径**: `POST /system/menu/listByPage`
- **描述**: 分页查询菜单或权限列表
- **请求参数**:
  - Body: [SysMenuPageDTO](#sysmenupagedto)

**请求示例**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sysMenu": {
    "isMenu": 1,
    "permissionCode": "system:user",
    "menuName": "用户",
    "menuStatus": 1
  }
}
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` ([ApiPage](#apipage)\<[SysMenuVO](#sysmenuvo)\>): 分页菜单数据
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "rows": [
      {
        "id": 1,
        "menuName": "用户管理",
        "permissionCode": "system:user:list",
        "routerPath": "/system/user",
        "parentId": 0,
        "menuType": 1,
        "menuStatus": 1,
        "componentPath": "system/user/index",
        "isExternalLink": 0,
        "iconPath": "user",
        "sort": 1,
        "createBy": 1,
        "createTime": "2024-01-01T12:00:00",
        "updateBy": 1,
        "updateTime": "2024-01-01T12:00:00",
        "isMenu": 1,
        "children": []
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  },
  "message": "success"
}
```

#### SysMenuPageDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| sysMenu | Query | 是 | 查询条件 |

##### Query 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| isMenu | Integer | 否 | 是否菜单 |
| permissionCode | String | 否 | 权限编号 |
| menuName | String | 否 | 菜单名称 |
| menuStatus | Integer | 否 | 菜单状态 |

#### SysMenuVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | BigInteger | 菜单ID |
| menuName | String | 菜单名称 |
| permissionCode | String | 权限编号 |
| routerPath | String | 路由路径 |
| parentId | BigInteger | 父菜单ID |
| menuType | Integer | 菜单类型 |
| menuStatus | Integer | 菜单状态 |
| componentPath | String | 组件路径 |
| isExternalLink | Integer | 是否拓展链接 |
| iconPath | String | 图标路径 |
| sort | Integer | 序号 |
| createBy | BigInteger | 创建人 |
| createTime | LocalDateTime | 创建时间 |
| updateBy | BigInteger | 更新人 |
| updateTime | LocalDateTime | 更新时间 |
| isMenu | Integer | 是否菜单 |
| children | List\<SysMenuVO\> | 子项 |

---

### 5. 查询菜单或权限树形数据

- **接口路径**: `POST /system/menu/listByTree`
- **描述**: 获取菜单或权限的树形结构
- **请求参数**:
  - Body: [SysMenuTreeDTO](#symenutreedto)

**请求示例**:
```json
{
  "isMenu": 1,
  "menuStatus": 1
}
```

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
      "children": [
        {
          "id": 2,
          "name": "用户管理"
        },
        {
          "id": 3,
          "name": "角色管理"
        }
      ]
    }
  ],
  "message": "success"
}
```

#### SysMenuTreeDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| isMenu | Integer | 否 | 是否菜单 |
| menuStatus | Integer | 否 | 菜单状态 |

---

### 6. 通过角色获取菜单或权限（角色选择用）

- **接口路径**: `POST /system/menu/listByTreeAsRoleSelection`
- **描述**: 获取菜单树形结构用于角色权限分配
- **请求参数**:
  - Body: [SysMenuTreeDTO](#symenutreedto)

**请求示例**:
```json
{
  "isMenu": 1,
  "menuStatus": 1
}
```

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
      "checked": true,
      "children": [
        {
          "id": 2,
          "name": "用户管理",
          "checked": true
        }
      ]
    }
  ],
  "message": "success"
}
```

---

### 7. 通过角色获取所有菜单

- **接口路径**: `GET /system/menu/listByRoleId/{roleId}`
- **描述**: 根据角色ID获取该角色的所有菜单
- **请求参数**:
  - roleId (BigInteger): 角色ID（路径参数）

**请求示例**:
```
GET /system/menu/listByRoleId/1
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (List<[SysMenuVO](#sysmenuvo)>): 菜单列表
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "permissionCode": "system",
      "routerPath": "/system",
      "parentId": 0,
      "menuType": 0,
      "menuStatus": 1,
      "componentPath": "Layout",
      "isExternalLink": 0,
      "iconPath": "system",
      "sort": 1,
      "createBy": 1,
      "createTime": "2024-01-01T12:00:00",
      "updateBy": null,
      "updateTime": null,
      "isMenu": 1,
      "children": []
    }
  ],
  "message": "success"
}
```