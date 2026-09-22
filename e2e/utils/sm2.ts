/**
 * SM2 加密工具 - 用于 E2E 测试中模拟前端的密码加密行为
 *
 * 与生产前端 Login.vue 完全一致：
 * 1) 使用 sm-crypto 的 doEncrypt 默认加密模式（C1C2C3）
 * 2) 输出前增加 '04' 前缀（Hutool/BC 解密需要未压缩椭圆曲线点前缀，见 sm-crypto#72）
 */
import { sm2 } from 'sm-crypto';

export function sm2Encrypt(publicKey: string, msg: string): string {
  return '04' + sm2.doEncrypt(msg, publicKey);
}