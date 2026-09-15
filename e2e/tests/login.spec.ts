import { test, expect } from '@playwright/test';
import { loginViaUI, loginViaAPI } from '../utils/auth';
import { TEST_USER, waitForMessage } from '../utils/helpers';

test.describe('登录模块', () => {

  test('1.1 未登录时应跳转到登录页', async ({ page }) => {
    await page.goto('/#/admin/home');
    // 应该被重定向到登录页或显示登录页内容
    await page.waitForTimeout(1000);
    const url = page.url();
    expect(url).toContain('/login');
  });

  test('1.2 登录页面应正确展示', async ({ page }) => {
    await page.goto('/#/login');
    // 检查登录卡片可见
    await expect(page.locator('.a-login-card')).toBeVisible();
    // 检查用户名、密码、验证码输入框存在
    await expect(page.locator('input').first()).toBeVisible();
    // 检查登录按钮存在
    await expect(page.locator('button', { hasText: /登录|Login/i })).toBeVisible();
    // 检查验证码图片存在
    await expect(page.locator('.captcha-img')).toBeVisible();
  });

  test('1.3 错误密码应显示错误提示', async ({ page }) => {
    await page.goto('/#/login');
    await page.waitForSelector('.a-login-card');

    await page.locator('.el-form-item').filter({ hasText: /用户名|Username/i }).locator('input').fill(TEST_USER.username);
    await page.locator('.el-form-item').filter({ hasText: /密码|Password/i }).locator('input').fill('wrong_password');
    await page.locator('.el-form-item').filter({ hasText: /验证码|Captcha/i }).locator('input').fill('0000');

    const loginBtn = page.locator('button').filter({ hasText: /登录|Login/i }).last();
    await loginBtn.click();

    // 应显示错误消息
    await expect(page.locator('.el-message--error')).toBeVisible({ timeout: 5000 });
  });

  test('1.4 正确凭据应成功登录并跳转到首页', async ({ page }) => {
    await loginViaUI(page, TEST_USER);
    // 成功后应在 admin 路由下
    expect(page.url()).toContain('/admin');
    // 检查页面标题包含首页
    await expect(page.locator('.welcome-title, h1, h2, h3').first()).toBeVisible();
  });
});

test.describe('登录后状态', () => {

  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('1.5 登录后访问 loginInfo 应返回用户信息', async ({ page }) => {
    const response = await page.request.get('http://localhost:9000/api/system/loginInfo');
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
  });

  test('1.6 登录后访问用户菜单应返回菜单列表', async ({ page }) => {
    const response = await page.request.get('http://localhost:9000/api/system/user/menus');
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBeTruthy();
  });
});
