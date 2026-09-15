/**
 * SM2 加密工具 - 用于 E2E 测试中模拟前端的密码加密行为
 * 前端使用 SM2 公钥加密密码，后端使用私钥解密
 */
import { sm2 } from 'sm-crypto';

// 测试环境预置的 SM2 密钥对（由 run-tests.sh 写入 Redis）
export const TEST_SM2_PUBLIC_KEY = 'e2e-test-public-key';
export const TEST_SM2_PRIVATE_KEY = 'e2e-test-private-key';

/**
 * 使用 SM2 公钥加密消息（C1C3C2 模式，与 Hutool 默认一致）
 */
export function sm2Encrypt(publicKey: string, msg: string): string {
  // sm-crypto 的 doEncrypt 默认使用 C1C3C2 模式
  return sm2.doEncrypt(msg, publicKey, 1);
}
