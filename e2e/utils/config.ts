/**
 * E2E 测试配置
 */

/** 后端服务地址 */
export const TEST_BASE_URL = process.env.TEST_BASE_URL || 'http://localhost:9093';

/** 测试用户密码（与 app-test.yml 中 myapp.password.strength.password 一致） */
export const TEST_PASSWORD = process.env.TEST_PASSWORD || 'Cyr3ne@2024!T3st';

/** 测试超时（毫秒） */
export const TEST_TIMEOUT = 30000;

/** API 请求超时（毫秒） */
export const API_TIMEOUT = 10000;

/** E2E 测试预置的 SM2 公钥（写入 Redis 用于登录解密） */
export const E2E_SM2_PUBLIC_KEY = 'e2e-test-public-key';
