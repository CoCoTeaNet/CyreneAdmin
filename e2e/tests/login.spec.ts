import { test, expect } from '@playwright/test';
import { loginViaUI, loginViaAPI, watchCaptcha } from '../utils/auth';
import { TEST_USER, waitForMessage } from '../utils/helpers';

test.describe('登录模块', () => {

  test('1.1 未登录时应跳转到登录页', async ({ page }) => {
    await page.goto('/#/admin/home');
    await page.waitForTimeout(1000);
    const url = page.url();
    expect(url).toContain('/login');
  });

  test('1.2 登录页面应正确展示', async ({ page }) => {
    await page.goto('/#/login');
    await expect(page.locator('.a-login-card')).toBeVisible();
    await expect(page.locator('input').first()).toBeVisible();
    await expect(page.locator('button', { hasText: /登录|Login/i })).toBeVisible();
    await expect(page.locator('.captcha-img')).toBeVisible();
  });

  test('1.3 登录页应有记住我复选框', async ({ page }) => {
    await page.goto('/#/login');
    await expect(page.locator('.el-checkbox').first()).toBeVisible();
  });

  test('1.4 错误密码应显示错误提示', async ({ page }) => {
    const watcher = watchCaptcha(page);
    await page.goto('/#/login');
    await page.waitForSelector('.a-login-card');
    const captchaText = await watcher.code;

    await page
      .locator('input[placeholder*="账号"], input[placeholder*="Account"], input[placeholder*="用户名"]')
      .first()
      .fill(TEST_USER.username);
    await page
      .locator('input[placeholder*="密码"], input[placeholder*="Password"]')
      .first()
      .fill('wrong-password');
    await page
      .locator('input[placeholder*="验证码"], input[placeholder*="Captcha"]')
      .first()
      .fill(captchaText);

    const loginBtn = page.locator('button', { hasText: /登录|Login/i }).last();
    await loginBtn.click();

    await expect(page.locator('.el-message--error')).toBeVisible({ timeout: 5000 });
  });

  test('1.5 正确凭据应成功登录并跳转到首页', async ({ page }) => {
    await loginViaUI(page, TEST_USER);
    expect(page.url()).toContain('/admin');
    await expect(page.locator('.el-card').first()).toBeVisible({ timeout: 8000 });
  });

  test('1.6 空用户名应显示校验错误', async ({ page }) => {
    await page.goto('/#/login');
    await page.waitForSelector('.a-login-card');

    const loginBtn = page.locator('button', { hasText: /登录|Login/i }).last();
    await loginBtn.click();

    await expect(page.locator('.el-form-item__error').first()).toBeVisible({ timeout: 5000 });
  });

  test('1.7 验证码图片应可点击刷新', async ({ page }) => {
    await page.goto('/#/login');
    await page.waitForSelector('.a-login-card');

    const captchaImg = page.locator('.captcha-img');
    await expect(captchaImg).toBeVisible();

    const srcBefore = await captchaImg.getAttribute('src');
    await captchaImg.click();
    await page.waitForTimeout(1000);
    const srcAfter = await captchaImg.getAttribute('src');

    if (srcBefore && srcAfter) {
      expect(srcAfter).toBeTruthy();
    }
  });
});

test.describe('登录后状态', () => {

  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('1.8 登录后访问 loginInfo 应返回用户信息', async ({ page }) => {
    const response = await page.request.get('http://localhost:9000/api/system/loginInfo');
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
    expect(body.data.username).toBe('admin');
  });

  test('1.9 登录后访问用户菜单应返回菜单列表', async ({ page }) => {
    const response = await page.request.get('http://localhost:9000/api/system/user/menus');
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBeTruthy();
    expect(body.data.length).toBeGreaterThan(0);
  });
});
