# CyreneAdmin Docker 部署指南

## 项目概述

CyreneAdmin 是一个基于 Solon 框架的后台管理系统，包含后端 API 服务和前端 Vue 应用。

## 构建和运行

### 使用 Docker Compose (推荐)

```bash
# 构建并启动整个应用栈（包括 MySQL 和 Redis）
docker-compose up -d

# 访问应用
# 前端: http://localhost
# 后端API: http://localhost:9000/api/
```

### 单独构建 Docker 镜像

```bash
# 构建镜像
docker build -t cyrene-admin .

# 运行容器（需要先启动数据库依赖）
docker run -d -p 80:80 -p 9000:9000 --name cyrene-admin cyrene-admin
```

## 架构说明

该 Docker 配置采用多阶段构建：

1. **Backend Builder**: 使用 Maven 构建后端 JAR 包
2. **Frontend Builder**: 使用 Node.js 构建前端静态资源
3. **Runtime**: 使用 Alpine Linux 运行时，同时运行 Nginx (前端) 和 Java (后端)

## 端口说明

- 端口 80: 前端应用访问端口
- 端口 9000: 后端 API 服务端口

## 配置说明

- 前端请求 `/api/` 路径会被代理到后端服务
- Nginx 配置了 CORS 头以支持跨域请求
- Supervisor 用于管理后端 Java 进程和 Nginx 进程

## 注意事项

1. 确保 MySQL 和 Redis 服务已正确配置
2. 生产环境请修改默认的数据库用户名和密码
3. 可通过环境变量调整应用配置

## 日志查看

```bash
# 查看应用日志
docker logs cyrene-admin

# 查看特定服务日志
docker exec -it cyrene-admin tail -f /var/log/supervisor/backend.log
docker exec -it cyrene-admin tail -f /var/log/supervisor/frontend.log
```