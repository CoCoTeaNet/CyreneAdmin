# SysDictionaryController API 接口文档

系统字典管理接口，提供字典的增删改查及树形结构查询功能。

## 基础信息

- **基地址**: `/system/dictionary`
- **描述**: 系统字典管理相关功能
- **权限要求**: 需要 `role:super:admin` 或 `role:simple:admin` 角色

## 接口详情

### 1. 新增字典

- **接口路径**: `POST /system/dictionary/add`
- **描述**: 新增字典项
- **请求参数**:
  - Body: [SysDictionaryAddDTO](#sysdictionaryadddto)

**请求示例**:
```json
{
  "parentId": 0,
  "dictionaryName": "性别",
  "remark": "用户性别字典",
  "sort": 1,
  "enableStatus": 1
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

#### SysDictionaryAddDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| parentId | BigInteger | 否 | 父节点主键ID |
| dictionaryName | String | 是 | 字典名称 |
| remark | String | 否 | 备注 |
| sort | Integer | 否 | 序号 |
| enableStatus | Integer | 否 | 启用状态;0关闭 1启用 |

---

### 2. 批量删除字典

- **接口路径**: `POST /system/dictionary/deleteBatch`
- **描述**: 批量删除字典项
- **请求参数**:
  - Body: List<BigInteger> 字典主键ID集合

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

### 3. 更新字典信息

- **接口路径**: `POST /system/dictionary/update`
- **描述**: 更新字典信息
- **请求参数**:
  - Body: [SysDictionaryUpdateDTO](#sysdictionaryupdatedto)

**请求示例**:
```json
{
  "id": 1,
  "parentId": 0,
  "dictionaryName": "性别更新",
  "remark": "用户性别字典更新",
  "sort": 2,
  "enableStatus": 1
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

#### SysDictionaryUpdateDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | BigInteger | 是 | 字典ID |
| parentId | BigInteger | 否 | 父节点主键ID |
| dictionaryName | String | 是 | 字典名称 |
| remark | String | 否 | 备注 |
| sort | Integer | 否 | 序号 |
| enableStatus | Integer | 否 | 启用状态;0关闭 1启用 |

---

### 4. 分页获取字典树形列表

- **接口路径**: `POST /system/dictionary/listByTree`
- **描述**: 获取字典树形结构列表
- **请求参数**:
  - Body: [SysDictionaryTreeDTO](#sysdictionarytreedto)

**请求示例**:
```json
{
  "sysDictionary": {
    "dictionaryName": "性别",
    "enableStatus": 1
  }
}
```

- **响应参数**:
  - `code` (Integer): 响应状态码
  - `data` (List<Tree\<BigInteger\>>): 字典树形结构数据
  - `message` (String): 响应消息

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "性别",
      "children": [
        {
          "id": 2,
          "name": "男"
        },
        {
          "id": 3,
          "name": "女"
        }
      ]
    }
  ],
  "message": "success"
}
```

#### SysDictionaryTreeDTO 结构

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| sysDictionary | [SysDictionaryVO](#sysdictionaryvo) | 否 | 查询条件 |

#### SysDictionaryVO 结构

| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | BigInteger | 字典ID |
| parentId | BigInteger | 父节点ID |
| dictionaryName | String | 字典名称 |
| remark | String | 备注 |
| sort | Integer | 排序序号 |
| enableStatus | Integer | 启用状态;0关闭 1启用 |
| createBy | String | 创建人 |
| createTime | LocalDateTime | 创建时间 |
| children | List\<SysDictionaryVO\> | 子项 |