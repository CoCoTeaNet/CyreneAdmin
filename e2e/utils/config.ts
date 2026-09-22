/**
 * E2E 测试配置
 */

/** 后端服务地址 */
export const TEST_BASE_URL = process.env.TEST_BASE_URL || 'http://localhost:9000';

/** 后端 API 根路径（contextPath=/api） */
export const API_URL = process.env.API_URL || `${TEST_BASE_URL}/api`;

/** 测试用户密码（与运行中的后端 app.yml 中 myapp.password 一致） */
export const TEST_PASSWORD = process.env.TEST_PASSWORD || 'admin#123456';

/** 测试超时（毫秒） */
export const TEST_TIMEOUT = 30000;

/** API 请求超时（毫秒） */
export const API_TIMEOUT = 10000;

/** Redis 配置（与运行中的后端对齐：127.0.0.1:6379, db=1） */
export const TEST_REDIS_HOST = process.env.TEST_REDIS_HOST || '127.0.0.1';
export const TEST_REDIS_PORT = Number(process.env.TEST_REDIS_PORT || 6379);
export const TEST_REDIS_DB = process.env.TEST_REDIS_DB || '1';