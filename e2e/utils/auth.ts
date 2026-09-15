/**
 * 认证工具 - 处理登录鉴权
 *
 * 生产环境前端流程：请求验证码 -> SM2加密密码 -> 提交登录
 * E2E 测试通过 API 获取公钥并加密密码，模拟完整前端登录流程
 */
import { type BrowserContext } from '@playwright/test';
import { TEST_BASE_URL, TEST_PASSWORD } from './config';
import { sm2Encrypt } from './sm2';

/**
 * 通过 API 快速登录并注入 token 到浏览器上下文
 * 流程：获取验证码（含SM2公钥） -> SM2加密密码 -> 提交登录 -> 注入satoken cookie
 */
export async function apiLogin(
  context: BrowserContext,
  username: string = 'admin',
  password: string = TEST_PASSWORD,
): Promise<void> {
  const apiContext = await context.request.newContext({
    baseURL: TEST_BASE_URL,
    ignoreHTTPSErrors: true,
  });

  try {
    // 1. 获取验证码（同时获取 SM2 公钥，私钥由 run-tests.sh 预置到 Redis）
    const captchaRes = await apiContext.get('/system/captcha', {
      params: { timestamp: Date.now().toString() },
    });
    const captchaBody = await captchaRes.json();
    const publicKey = captchaBody.data?.publicKey;

    if (!publicKey) {
      throw new Error('Failed to get SM2 public key from captcha endpoint');
    }

    // 2. SM2 加密密码（模拟前端行为）
    const encryptedPassword = sm2Encrypt(publicKey, password);

    // 3. 提交登录
    const loginRes = await apiContext.post('/api/login', {
      data: { username, password: encryptedPassword, publicKey },
    });
    const loginBody = await loginRes.json();

    if (loginBody.code !== 200 || !loginBody.data) {
      console.error('API login failed:', JSON.stringify(loginBody));
      throw new Error(`API login failed: ${loginBody.msg || 'unknown error'}`);
    }

    // 4. 注入 token 到浏览器上下文
    await context.addCookies([
      {
        name: 'satoken',
        value: loginBody.data,
        domain: new URL(TEST_BASE_URL).hostname,
        path: '/',
        httpOnly: false,
        secure: false,
        sameSite: 'Lax',
      },
    ]);
  } finally {
    await apiContext.dispose();
  }
}

/**
 * 通过 UI 表单登录（用于测试登录功能本身）
 */
export async function uiLogin(
  context: BrowserContext,
  username: string = 'admin',
  password: string = TEST_PASSWORD,
): Promise<void> {
  const page = await context.newPage();
  await page.goto(`${TEST_BASE_URL}/`);
  await page.waitForLoadState('networkidle');
  await page.waitForSelector('.login-container', { timeout: 10000 });
  await page.fill('.el-form-item:nth-child(1) .el-input__inner', username);
  await page.fill('.el-form-item:nth-child(2) .el-input__inner', password);
  await page.waitForTimeout(500);
  await page.click('.el-button--primary');
  await page.waitForURL('**/dashboard', { timeout: 15000 });
  await page.close();
}
