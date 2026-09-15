#!/usr/bin/env bash
# ============================================================
#  CyreneAdmin E2E 一键测试脚本
#
#  用法:
#    ./e2e/run-tests.sh              # 完整流程: 启动基础设施 -> 构建 -> 测试 -> 清理
#    ./e2e/run-tests.sh --skip-build # 跳过构建（已有构建产物时使用）
#    ./e2e/run-tests.sh --headed     # 有头模式运行（可视化调试）
#    ./e2e/run-tests.sh --api-only   # 仅运行 API 测试（不需要浏览器）
#
#  日志: 所有输出写入 /tmp/cyrene-e2e-run.log
# ============================================================
set -uo pipefail

# ---------- 日志文件 ----------
RUN_LOG="/tmp/cyrene-e2e-run.log"
> "$RUN_LOG"  # 清空上次日志

# ---------- 日志系统：同时输出到终端和文件 ----------
ts() { date '+%H:%M:%S'; }

info()  { local msg="[$(ts)] [INFO]  $*"; echo -e "\033[0;36m${msg}\033[0m" | tee -a "$RUN_LOG"; }
ok()    { local msg="[$(ts)] [OK]    $*"; echo -e "\033[0;32m${msg}\033[0m" | tee -a "$RUN_LOG"; }
warn()  { local msg="[$(ts)] [WARN]  $*"; echo -e "\033[1;33m${msg}\033[0m" | tee -a "$RUN_LOG"; }
fail()  { local msg="[$(ts)] [FAIL]  $*"; echo -e "\033[0;31m${msg}\033[0m" | tee -a "$RUN_LOG"; }

# 运行命令并同时记录输出到日志
run_cmd() {
  local desc="$1"; shift
  info ">>> $desc"
  echo "[$(ts)] [CMD] $*" >> "$RUN_LOG"
  "$@" >> "$RUN_LOG" 2>&1
  local rc=$?
  if [[ $rc -ne 0 ]]; then
    fail "$desc 失败 (exit=$rc)"
    echo "--- 最后 30 行日志 ---" >> "$RUN_LOG"
    tail -30 "$RUN_LOG"
    return $rc
  fi
  ok "$desc 完成"
  return 0
}

# ---------- 变量 ----------
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
E2E_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/cyrene-starter-solon"
FRONTEND_DIR="$ROOT_DIR/cyrene-ui"

BACKEND_PORT=9000
FRONTEND_PORT=8080
MYSQL_PORT=13306
REDIS_PORT=16379

BACKEND_PID=""
FRONTEND_PID=""
INFRA_STARTED=false
SKIP_BUILD=false
HEADED_FLAG=""
API_ONLY=false
STEP_FAILED=""

# ---------- ERR trap：捕获任何意外失败 ----------
err_handler() {
  local line="$1"
  local cmd="$2"
  local code="$3"
  echo ""
  fail "========================================="
  fail "  脚本在第 $line 行意外退出"
  fail "  执行的命令: $cmd"
  fail "  退出码: $code"
  fail "========================================="
  show_log_hint
}
trap 'err_handler ${LINENO} "$BASH_COMMAND" $?' ERR

# ---------- 参数解析 ----------
while [[ $# -gt 0 ]]; do
  case $1 in
    --skip-build)  SKIP_BUILD=true; shift ;;
    --headed)      HEADED_FLAG="--headed"; shift ;;
    --api-only)    API_ONLY=true; shift ;;
    *)             warn "未知参数: $1"; shift ;;
  esac
done

# ---------- 清理函数 ----------
cleanup() {
  local exit_code=$?
  echo "" | tee -a "$RUN_LOG"
  info "正在清理..."

  # 停止前端进程
  if [[ -n "$FRONTEND_PID" ]] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
    kill "$FRONTEND_PID" 2>/dev/null || true
    wait "$FRONTEND_PID" 2>/dev/null || true
    ok "前端服务已停止 (PID: $FRONTEND_PID)"
  fi
  # 停止后端进程
  if [[ -n "$BACKEND_PID" ]] && kill -0 "$BACKEND_PID" 2>/dev/null; then
    kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
    ok "后端服务已停止 (PID: $BACKEND_PID)"
  fi
  # 停止基础设施容器
  if [[ "$INFRA_STARTED" == true ]]; then
    info "停止 Docker 容器..."
    cd "$ROOT_DIR"
    docker compose -f docker-compose.test.yml stop mysql redis 2>/dev/null || true
    ok "Docker 容器已停止"
  fi

  # 失败时自动展示日志提示
  if [[ $exit_code -ne 0 ]] || [[ -n "$STEP_FAILED" ]]; then
    show_log_hint
  fi
}
trap cleanup EXIT

show_log_hint() {
  echo "" | tee -a "$RUN_LOG"
  fail "========================================="
  fail "  查看完整日志: cat $RUN_LOG"
  fail "  查看后端日志: tail -100 /tmp/cyrene-backend.log"
  fail "  查看前端日志: tail -50 /tmp/cyrene-frontend.log"
  fail "========================================="
}

# ---------- 环境检查 ----------
check_prerequisites() {
  info "检查运行环境..."
  local missing=false

  if ! command -v java &>/dev/null; then
    fail "未找到 java，请安装 JDK 17+"
    missing=true
  else
    info "Java: $(java -version 2>&1 | head -1)"
  fi

  if ! command -v mvn &>/dev/null; then
    fail "未找到 mvn，请安装 Maven"
    missing=true
  else
    info "Maven: $(mvn -v 2>&1 | head -1)"
  fi

  if ! command -v node &>/dev/null; then
    fail "未找到 node，请安装 Node.js 18+"
    missing=true
  else
    info "Node: $(node -v)"
  fi

  if ! command -v docker &>/dev/null; then
    warn "未找到 docker，将尝试连接外部 MySQL/Redis"
  fi

  if [[ "$missing" == true ]]; then
    fail "缺少必要依赖，退出"
    exit 1
  fi

  ok "环境检查通过"
  echo "--- 环境信息 ---" >> "$RUN_LOG"
  echo "ROOT_DIR=$ROOT_DIR" >> "$RUN_LOG"
  echo "E2E_DIR=$E2E_DIR" >> "$RUN_LOG"
  echo "---" >> "$RUN_LOG"
}

# ---------- 启动基础设施 (MySQL + Redis) ----------
start_infrastructure() {
  info "启动 MySQL + Redis..."

  # 检查是否已有外部服务运行
  if nc -z 127.0.0.1 "$MYSQL_PORT" 2>/dev/null && nc -z 127.0.0.1 "$REDIS_PORT" 2>/dev/null; then
    ok "MySQL ($MYSQL_PORT) 和 Redis ($REDIS_PORT) 已运行，跳过启动"
    return 0
  fi

  # 创建临时的 docker-compose 测试配置
  cat > "$ROOT_DIR/docker-compose.test.yml" <<'YAML'
services:
  mysql:
    image: mysql:8.0.45
    ports:
      - "13306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: test123456
      MYSQL_DATABASE: cyrene_admin
      TZ: Asia/Shanghai
    volumes:
      - ./scripts/ddl.sql:/docker-entrypoint-initdb.d/01-ddl.sql
      - ./scripts/init-data-test.sql:/docker-entrypoint-initdb.d/02-init-data.sql
      - ./scripts/init-menu-zh.sql:/docker-entrypoint-initdb.d/03-init-menu.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-ptest123456"]
      interval: 5s
      timeout: 5s
      retries: 20
    command: --default-authentication-plugin=mysql_native_password

  redis:
    image: redis:7-alpine
    ports:
      - "16379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 3s
      timeout: 3s
      retries: 10
YAML

  cd "$ROOT_DIR"
  info "拉取镜像并启动容器..."
  docker compose -f docker-compose.test.yml up -d mysql redis >> "$RUN_LOG" 2>&1
  local rc=$?
  if [[ $rc -ne 0 ]]; then
    fail "docker compose up 失败 (exit=$rc)"
    docker compose -f docker-compose.test.yml logs >> "$RUN_LOG" 2>&1 || true
    exit 1
  fi
  INFRA_STARTED=true

  # 等待服务就绪
  info "等待 MySQL 就绪..."
  local retries=30
  while ! docker compose -f docker-compose.test.yml exec -T mysql mysqladmin ping -h localhost -uroot -ptest123456 --silent 2>/dev/null; do
    retries=$((retries - 1))
    if [[ $retries -le 0 ]]; then
      fail "MySQL 启动超时，容器日志:"
      docker compose -f docker-compose.test.yml logs mysql 2>&1 | tail -30 | tee -a "$RUN_LOG"
      exit 1
    fi
    echo -n "." | tee -a "$RUN_LOG"
    sleep 2
  done
  echo "" | tee -a "$RUN_LOG"
  ok "MySQL 就绪"

  # 验证 SQL 初始化是否成功
  info "验证数据库表..."
  docker compose -f docker-compose.test.yml exec -T mysql mysql -uroot -ptest123456 cyrene_admin -e "SHOW TABLES;" >> "$RUN_LOG" 2>&1
  if [[ $? -ne 0 ]]; then
    fail "数据库表验证失败"
    docker compose -f docker-compose.test.yml exec -T mysql mysql -uroot -ptest123456 cyrene_admin -e "SHOW TABLES;" 2>&1 | tee -a "$RUN_LOG"
    exit 1
  fi
  ok "数据库表已就绪"

  info "等待 Redis 就绪..."
  retries=15
  while ! docker compose -f docker-compose.test.yml exec -T redis redis-cli ping 2>/dev/null | grep -q PONG; do
    retries=$((retries - 1))
    if [[ $retries -le 0 ]]; then
      fail "Redis 启动超时"
      exit 1
    fi
    echo -n "." | tee -a "$RUN_LOG"
    sleep 1
  done
  echo "" | tee -a "$RUN_LOG"
  ok "Redis 就绪"
}

# ---------- 构建后端 ----------
build_backend() {
  if [[ "$SKIP_BUILD" == true ]] && [[ -f "$BACKEND_DIR/target/launcher.jar" ]]; then
    ok "跳过后端构建（使用已有 JAR）"
    return 0
  fi

  info "构建后端 (Maven, test profile)..."
  cd "$ROOT_DIR"
  mvn clean package -pl cyrene-starter-solon -am -DskipTests 2>&1 | tee -a "$RUN_LOG" | tail -20

  if [[ ! -f "$BACKEND_DIR/target/launcher.jar" ]]; then
    fail "后端构建失败: launcher.jar 未生成"
    exit 1
  fi
  ok "后端构建成功 ($(du -h "$BACKEND_DIR/target/launcher.jar" | cut -f1))"
}

# ---------- 构建前端 ----------
build_frontend() {
  if [[ "$SKIP_BUILD" == true ]] && [[ -d "$FRONTEND_DIR/dist" ]]; then
    ok "跳过前端构建（使用已有产物）"
    return 0
  fi

  info "构建前端 (pnpm)..."
  cd "$FRONTEND_DIR"
  if [[ ! -d "node_modules" ]]; then
    pnpm install --frozen-lockfile 2>&1 | tee -a "$RUN_LOG" | tail -5
  fi
  pnpm run build 2>&1 | tee -a "$RUN_LOG" | tail -10
  if [[ ! -d "dist" ]]; then
    fail "前端构建失败: dist 目录未生成"
    exit 1
  fi
  ok "前端构建成功"
}

# ---------- 启动后端 ----------
start_backend() {
  info "启动后端服务 (Solon, port $BACKEND_PORT, test profile)..."
  cd "$ROOT_DIR"

  # 清理旧日志
  > /tmp/cyrene-backend.log

  nohup java -Xms256m -Xmx512m \
    -Dsolon.profiles.active=test \
    -jar "$BACKEND_DIR/target/launcher.jar" \
    --myapp.db1.jdbcUrl="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/cyrene_admin?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&tinyInt1isBit=false" \
    --myapp.db1.username=root \
    --myapp.db1.password=test123456 \
    --myapp.rd1.server="127.0.0.1:${REDIS_PORT}" \
    >> /tmp/cyrene-backend.log 2>&1 &
  disown
  BACKEND_PID=$!

  echo "[$(ts)] [INFO]  后端 PID: $BACKEND_PID" >> "$RUN_LOG"
  info "等待后端启动 (PID: $BACKEND_PID)..."

  # 阶段 1: 等待进程存活
  sleep 2
  if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    fail "后端进程启动后立即退出"
    echo "--- 后端日志 (全部) ---" | tee -a "$RUN_LOG"
    cat /tmp/cyrene-backend.log | tee -a "$RUN_LOG"
    exit 1
  fi

  # 阶段 2: 等待 TCP 端口
  local retries=40
  while ! nc -z 127.0.0.1 "$BACKEND_PORT" 2>/dev/null; do
    retries=$((retries - 1))
    if [[ $retries -le 0 ]]; then
      fail "后端启动超时 (${BACKEND_PORT} 端口未监听)"
      echo "--- 后端日志 (最后 80 行) ---" | tee -a "$RUN_LOG"
      tail -80 /tmp/cyrene-backend.log | tee -a "$RUN_LOG"
      exit 1
    fi
    # 检查进程是否还在运行
    if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
      fail "后端进程意外退出"
      echo "--- 后端日志 (全部) ---" | tee -a "$RUN_LOG"
      cat /tmp/cyrene-backend.log | tee -a "$RUN_LOG"
      exit 1
    fi
    echo -n "." | tee -a "$RUN_LOG"
    sleep 2
  done
  echo "" | tee -a "$RUN_LOG"

  # 阶段 3: 等待 Solon 内部初始化完成
  info "端口已就绪，等待应用初始化..."
  sleep 3

  # 验证后端 API 可访问
  local api_ok=false
  for i in 1 2 3; do
    if curl -sf -o /dev/null "http://127.0.0.1:${BACKEND_PORT}/api/system/captcha" 2>/dev/null; then
      api_ok=true
      break
    fi
    sleep 1
  done
  if [[ "$api_ok" == true ]]; then
    ok "后端 API 可访问"
  else
    warn "后端 API 暂时不可访问（可能仍在初始化），继续..."
  fi

  # 预置测试验证码到 Redis
  docker compose -f docker-compose.test.yml exec -T redis redis-cli SET "captcha:e2e-test-captcha" "1" EX 3600 >> "$RUN_LOG" 2>&1 \
    && ok "Redis 预置验证码完成" || warn "Redis 预置验证码跳过"

  ok "后端服务已就绪 (http://127.0.0.1:${BACKEND_PORT})"
}

# ---------- 启动前端 ----------
start_frontend() {
  info "启动前端开发服务器 (Vite, port $FRONTEND_PORT)..."
  cd "$FRONTEND_DIR"

  # 清理旧日志
  > /tmp/cyrene-frontend.log

  if [[ ! -d "node_modules" ]]; then
    pnpm install --frozen-lockfile 2>&1 | tee -a "$RUN_LOG" | tail -5
  fi

  nohup pnpm run dev >> /tmp/cyrene-frontend.log 2>&1 &
  disown
  FRONTEND_PID=$!

  echo "[$(ts)] [INFO]  前端 PID: $FRONTEND_PID" >> "$RUN_LOG"
  info "等待前端启动 (PID: $FRONTEND_PID)..."

  # 先检查进程存活
  sleep 2
  if ! kill -0 "$FRONTEND_PID" 2>/dev/null; then
    fail "前端进程启动后立即退出"
    echo "--- 前端日志 (全部) ---" | tee -a "$RUN_LOG"
    cat /tmp/cyrene-frontend.log | tee -a "$RUN_LOG"
    exit 1
  fi

  # 等待 TCP 端口
  local retries=30
  while ! nc -z 127.0.0.1 "$FRONTEND_PORT" 2>/dev/null; do
    retries=$((retries - 1))
    if [[ $retries -le 0 ]]; then
      fail "前端启动超时 (${FRONTEND_PORT} 端口未监听)"
      echo "--- 前端日志 (最后 30 行) ---" | tee -a "$RUN_LOG"
      tail -30 /tmp/cyrene-frontend.log | tee -a "$RUN_LOG"
      exit 1
    fi
    if ! kill -0 "$FRONTEND_PID" 2>/dev/null; then
      fail "前端进程意外退出"
      echo "--- 前端日志 (全部) ---" | tee -a "$RUN_LOG"
      cat /tmp/cyrene-frontend.log | tee -a "$RUN_LOG"
      exit 1
    fi
    echo -n "." | tee -a "$RUN_LOG"
    sleep 1
  done
  echo "" | tee -a "$RUN_LOG"
  ok "前端服务已就绪 (http://127.0.0.1:${FRONTEND_PORT})"
}

# ---------- 安装 Playwright ----------
install_playwright() {
  cd "$E2E_DIR"
  if [[ ! -d "node_modules" ]]; then
    info "安装 E2E 测试依赖..."
    npm install 2>&1 | tee -a "$RUN_LOG" | tail -5
  fi
  info "确保 Playwright 浏览器已安装..."
  npx playwright install chromium 2>&1 | tee -a "$RUN_LOG" | tail -5
  ok "Playwright 就绪"
}

# ---------- 运行测试 ----------
run_tests() {
  cd "$E2E_DIR"
  info "========================================="
  info "  运行 E2E 测试"
  info "========================================="

  export BASE_URL="http://127.0.0.1:${FRONTEND_PORT}"
  export API_URL="http://127.0.0.1:${BACKEND_PORT}/api"

  local test_args=""
  if [[ -n "$HEADED_FLAG" ]]; then
    test_args="--headed"
  fi
  if [[ "$API_ONLY" == true ]]; then
    test_args="$test_args tests/api-integration.spec.ts"
  fi

  # 清除 ERR trap 以免 playwright 的非零退出触发它
  trap - ERR

  set +e
  npx playwright test $test_args --reporter=list 2>&1 | tee -a "$RUN_LOG"
  local exit_code=${PIPESTATUS[0]}
  set -e

  # 恢复 ERR trap
  trap 'err_handler ${LINENO} "$BASH_COMMAND" $?' ERR

  echo ""
  if [[ $exit_code -eq 0 ]]; then
    ok "========================================="
    ok "  所有测试通过!"
    ok "========================================="
  else
    fail "========================================="
    fail "  部分测试失败 (exit code: $exit_code)"
    fail "========================================="
    info "查看 HTML 报告: cd e2e && npx playwright show-report"
  fi

  return $exit_code
}

# ========== 主流程 ==========
main() {
  echo ""
  echo "============================================"
  echo "  CyreneAdmin E2E 自动化测试"
  echo "  日志文件: $RUN_LOG"
  echo "============================================"
  echo ""

  info "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"
  local start_time=$(date +%s)

  check_prerequisites
  start_infrastructure

  if [[ "$SKIP_BUILD" != true ]]; then
    build_backend
    build_frontend
  fi

  start_backend
  start_frontend
  install_playwright

  local exit_code=0
  run_tests || exit_code=$?

  local end_time=$(date +%s)
  local duration=$((end_time - start_time))
  local mins=$((duration / 60))
  local secs=$((duration % 60))

  echo "" | tee -a "$RUN_LOG"
  info "结束时间: $(date '+%Y-%m-%d %H:%M:%S')"
  info "总耗时: ${mins}m ${secs}s"
  info "完整日志: $RUN_LOG"

  exit $exit_code
}

main "$@"
