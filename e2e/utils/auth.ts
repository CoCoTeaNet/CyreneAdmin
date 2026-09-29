/**
 * 认证工具 - 处理登录鉴权
 *
 * 生产环境前端流程：请求验证码 -> SM2加密密码 -> 提交登录
 * E2E 通过 API 获取验证码与公钥，从 Redis 读取验证码文本后走完整登录链路。
 * 注意：登录链路强制校验 验证码 + 验证码ID + SM2公钥 三件套（移除 strong-password 后的契约）。
 */
import { type BrowserContext, type Page } from '@playwright/test';
import { API_URL, PAGE_BASE_URL, TEST_PASSWORD } from './config';
import { sm2Encrypt } from './sm2';
import { getVerifyCode } from './redis';

export interface LoginCredential {
  username: string;
  password: string;
}

/**
 * 监听页面内的 /system/captcha 响应，并返回 Promise<验证码文本>。
 * 必须在 page.goto 之前调用（登录页 onMounted 时已发起验证码请求）。
 */
export function watchCaptcha(page: Page): { code: Promise<string> } {
  let resolveCode: (v: string) => void;
  const code = new Promise<string>((res) => {
    resolveCode = res;
  });

  page.on('response', (resp) => {
    if (resp.url().includes('/system/captcha')) {
      resp
        .json()
        .then((j) => {
          const captchaId = j?.data?.captchaId;
          if (captchaId) {
            getVerifyCode(captchaId).then(resolveCode).catch(() => {});
          }
        })
        .catch(() => {});
    }
  });

  return { code };
}

/**
 * 通过 API 完成完整登录（验证码 -> SM2加密 -> 提交登录），返回 token
 */
export async function apiLoginRequest(
  request: any,
  username: string = 'admin',
  password: string = TEST_PASSWORD,
): Promise<string> {
  // 1. 获取验证码（含 SM2 公钥与验证码ID）
  const captchaRes = await request.get(`${API_URL}/system/captcha?timestamp=${Date.now()}`);
  const captchaBody = await captchaRes.json();
  const { captchaId, publicKey } = captchaBody?.data ?? {};
  if (!captchaId || !publicKey) {
    throw new Error(`验证码接口响应异常: ${JSON.stringify(captchaBody)}`);
  }

  // 2. 从 Redis 读取验证码文本（与真实用户看图输入等价）
  const captchaText = await getVerifyCode(captchaId);

  // 3. SM2 加密密码
  const encryptedPassword = sm2Encrypt(publicKey, password);

  // 4. 提交登录（携带完整三件套）
  const loginRes = await request.post(`${API_URL}/system/login`, {
    data: {
      username,
      password: encryptedPassword,
      captcha: captchaText,
      captchaId,
      publicKey,
      rememberMe: false,
    },
  });
  const loginBody = await loginRes.json();

  if (loginBody.code !== 200 || !loginBody.data) {
    console.error('API login failed:', JSON.stringify(loginBody));
    throw new Error(`API 登录失败: ${loginBody.msg || loginBody.message || 'unknown error'}`);
  }

  return loginBody.data;
}

/**
 * API 登录并注入 token 到浏览器上下文（供 UI 页面测试使用）
 */
export async function apiLogin(
  context: BrowserContext,
  username: string = 'admin',
  password: string = TEST_PASSWORD,
): Promise<void> {
  const token = await apiLoginRequest(context.request, username, password);
  // Cookie 按 host 匹配（与端口无关）：页面源与 API 源可能不同 host
  // （ps1: BASE_URL=127.0.0.1 而 API_URL=localhost；sh: 同为 127.0.0.1）。
  // 只注入 API host 时，页面同源请求不携带 Authorization → 后端 4001
  // “未能读取到有效 token” → 前端跳回登录页（表现为“无法登录”）。
  // 因此按 hostname 去重后，为页面 host 与 API host 各注入一份 host-only cookie。
  const hosts = [...new Set([
    new URL(PAGE_BASE_URL).hostname,
    new URL(API_URL).hostname,
  ])];
  await context.addCookies(hosts.map((host) => ({
    name: 'Authorization',
    value: token,
    url: `http://${host}`,
    httpOnly: false,
    secure: false,
    sameSite: 'Lax' as const,
  })));
}

/**
 * 通过 UI 表单登录（用于测试登录页本身）
 * 流程：进入登录页 -> 监听验证码响应 -> 从 Redis 读取验证码 -> 填写表单提交
 */
export async function uiLogin(
  page: Page,
  user: Partial<LoginCredential> = {},
): Promise<void> {
  const username = user.username || 'admin';
  const password = user.password || TEST_PASSWORD;

  const watcher = watchCaptcha(page);
  await page.goto('/#/login');
  await page.waitForSelector('.a-login-card', { timeout: 10000 });

  const captchaText = await watcher.code;

  await page
    .locator('input[placeholder*="账号"], input[placeholder*="Account"], input[placeholder*="用户名"]')
    .first()
    .fill(username);
  await page
    .locator('input[placeholder*="密码"], input[placeholder*="Password"]')
    .first()
    .fill(password);
  await page
    .locator('input[placeholder*="验证码"], input[placeholder*="Captcha"]')
    .first()
    .fill(captchaText);

  const loginBtn = page.locator('button').filter({ hasText: /登录|Login/i }).last();
  await loginBtn.click();

  await page.waitForURL(/#\/admin/, { timeout: 15000 });
}

/** 兼容历史命名 */
export const loginViaAPI = apiLogin;
export const loginViaUI = uiLogin;

/**
 * 导航到指定前端页面（hash 路由）
 * 先访问 home 初始化 tab 状态（App.vue onMounted 会调用 loadTabItems，
 * 若无缓存 tab 会 router.push('/admin/home') 覆盖目标路由），再跳转到目标页。
 */
export async function navigateTo(page: Page, path: string): Promise<void> {
  await page.goto('/#/admin/home');
  await page.waitForLoadState('domcontentloaded');
  await page.waitForTimeout(1000);
  await page.goto(`/#${path}`);
  await page.waitForLoadState('domcontentloaded');
  await page.waitForTimeout(1500);
}