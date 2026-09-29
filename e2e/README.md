# CyreneAdmin E2E 自动化测试

基于 [Playwright](https://playwright.dev/) 的端到端测试套件，覆盖前后端整体功能。

## 目录结构

```
e2e/
├── run-tests.sh                    # 一键测试脚本
├── package.json                    # 测试依赖
├── playwright.config.ts            # Playwright 配置
├── tsconfig.json                   # TypeScript 配置
├── utils/
│   ├── auth.ts                     # 登录辅助（UI/API两种方式）
│   └── helpers.ts                  # 通用 UI 操作封装
└── tests/
    ├── login.spec.ts               # 登录模块测试
    ├── navigation.spec.ts          # 导航与布局测试
    ├── dashboard.spec.ts           # 仪表盘测试
    ├── user-management.spec.ts     # 用户管理 CRUD 测试
    ├── role-management.spec.ts     # 角色管理 CRUD 测试
    ├── menu-management.spec.ts     # 菜单管理测试
    ├── dictionary-management.spec.ts # 字典管理测试
    ├── log-management.spec.ts      # 日志管理测试
    ├── logout.spec.ts              # 登出测试
    └── api-integration.spec.ts     # 后端 API 接口测试
```

## 一键运行

### 前置条件

- JDK 17+
- Maven 3.9+
- Node.js 18+ & pnpm
- Docker (可选，用于 MySQL/Redis)

### 完整流程

```bash
./e2e/run-tests.sh
```

自动完成：启动基础设施 → 构建后端/前端 → 启动项目 → 运行测试 → 清理。

### 其他模式

```bash
# 跳过构建（已有产物时加速）
./e2e/run-tests.sh --skip-build

# 有头模式（可视化调试）
./e2e/run-tests.sh --headed

# 仅 API 测试（不需要浏览器）
./e2e/run-tests.sh --api-only
```

## 手动运行

```bash
# 1. 启动 MySQL 和 Redis (使用 e2e/docker-compose.test.yml，项目名固定为 e2e)
docker compose -f e2e/docker-compose.test.yml up -d

# 2. 构建并启动后端
mvn clean package -pl cyrene-starter-solon -am -DskipTests
java -jar cyrene-starter-solon/target/launcher.jar \
  --solon.profiles.active=test \
  --myapp.captcha.enabled=false

# 3. 启动前端
cd cyrene-ui && pnpm install && pnpm dev

# 4. 安装依赖并运行测试
cd e2e && npm install
npx playwright install chromium
BASE_URL=http://localhost:8080 API_URL=http://localhost:9000/api npx playwright test
```

## 查看测试报告

```bash
cd e2e && npx playwright show-report
```

## 故障排查

### `Conflict. The container name "/e2e-redis" is already in use`

**原因**：固定容器名（`e2e-mysql`/`e2e-redis`）属于 compose 项目名不变量。若同名容器由其它项目创建（如曾在仓库根目录用旧版 compose 文件 `up` 过，其 `com.docker.compose.project` 为 `cyreneadmin` 而非 `e2e`），compose 无法接管，`up` 即报 Conflict。

**处理**（两个脚本已内置自愈，正常重跑即可）：

```bash
# 查看容器归属
docker inspect e2e-redis --format '{{index .Config.Labels "com.docker.compose.project"}}'
# 若输出不是 e2e，移除残留容器后重跑
docker rm -f e2e-mysql e2e-redis
docker compose -f e2e/docker-compose.test.yml up -d
```

### MySQL 初始化后表为空

**原因**：`e2e/docker-compose.test.yml` 挂载的是 `../scripts/*.sql`（仓库根 `scripts/`）。若误将 `e2e/scripts/` 下的 `.sql` 创建成目录（而非文件），容器内 `/docker-entrypoint-initdb.d/` 会挂到空目录，init 脚本不执行。

**处理**：确认 `scripts/ddl.sql` 等为真实文件，删除重建容器重新初始化：

```bash
docker rm -f e2e-mysql
docker compose -f e2e/docker-compose.test.yml up -d mysql
```

### 登录成功但页面仍提示 `未能读取到有效 token`（4001，跳回登录页）

**原因**：`apiLogin` 注入的 `Authorization` cookie 按 host 匹配（与端口无关）。ps1 中页面源是 `127.0.0.1:5173`，而旧代码只按 `API_URL` 的 host（`localhost`）注入 cookie，页面同源请求不携带 Authorization → 后端 4001 → 前端跳回登录页。

**处理**（脚本已修复，正常重跑即可）：`e2e/utils/auth.ts` 现按「页面 host + API host」去重后各注入一份 host-only cookie；`run-tests.ps1` 同时导出 `API_URL`（原 `API_BASE` 是无效变量，`utils/config.ts` 读的是 `API_URL`）。确认两端环境变量一致：

```powershell
# ps1: BASE_URL=http://127.0.0.1:5173, API_URL=http://localhost:9000/api
# sh : BASE_URL=http://127.0.0.1:5173, API_URL=http://127.0.0.1:9000/api
```

### `[FAIL] frontend did not start on port 5173 within 60s`（但 `frontend.log` 显示 Vite ready）

**原因**：Windows 上 `localhost` 默认先解析为 IPv6 `::1`（dns verbatim 顺序）。旧脚本 `-Headed` 分支启动 Vite 时未传 `--host 127.0.0.1`，dev server 只监听 `[::1]:5173`，而就绪探测只连 `127.0.0.1`，必然超时——Vite 其实已启动成功。

**处理**（脚本已修复，正常重跑即可）：

- 启动 Vite 固定 `--host 127.0.0.1`；`WaitForPort` 双栈探测 `127.0.0.1` 与 `::1`；
- 等待期间若进程提前退出会快速失败，并自动输出 `frontend.log` / `frontend-err.log` 末尾日志；
- 启动前自动清理上次残留的 pid 文件与占端口进程，避免干扰就绪判断。

手动验证监听地址：

```powershell
netstat -ano | findstr 5173   # 应看到 127.0.0.1:5173
```

## 测试覆盖范围

| 模块 | 测试文件 | 用例数 | 说明 |
|------|----------|--------|------|
| 登录 | login.spec.ts | 6 | 页面展示、错误处理、登录流程、状态验证 |
| 导航 | navigation.spec.ts | 9 | 侧边栏、路由跳转、404页面 |
| 仪表盘 | dashboard.spec.ts | 3 | 页面加载、CPU/系统信息展示 |
| 用户管理 | user-management.spec.ts | 6 | 列表、搜索、新增/编辑弹窗、翻页 |
| 角色管理 | role-management.spec.ts | 4 | 列表、搜索、新增弹窗 |
| 菜单管理 | menu-management.spec.ts | 3 | 页面加载、新增按钮、权限页面 |
| 字典管理 | dictionary-management.spec.ts | 2 | 页面加载、新增按钮 |
| 日志管理 | log-management.spec.ts | 2 | 列表加载、数据验证 |
| 登出 | logout.spec.ts | 1 | 登出后跳转 |
| API接口 | api-integration.spec.ts | 8 | 全部28个API的接口级别测试 |

**共计 44 个测试用例**

## 测试环境配置

后端测试环境使用 `app-test.yml`，主要差异：

- 数据库端口: `13306`（避免冲突）
- Redis 端口: `16379`（避免冲突）
- 验证码: `myapp.captcha.enabled=false`（自动放行）
- 密码: 明文传递，跳过 SM2 加密

## 架构设计

1. **后端最小改动**: 仅在 `SysLoginController` 中增加 `TestModeProperties` 配置判断，test profile 下自动跳过验证码和 SM2 解密
2. **双模登录**: 支持 UI 登录（模拟真实用户操作）和 API 登录（快速注入 token 到 localStorage）
3. **独立进程管理**: 测试脚本通过 PID 管理所有子进程，Ctrl+C 后自动清理
4. **CI 友好**: 支持 `--skip-build` 和无头模式，可直接集成到 CI/CD 流水线
