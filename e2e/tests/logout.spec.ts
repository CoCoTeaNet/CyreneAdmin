import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';

test.describe('登出', () => {
  test('9.1 通过 UI 登出应返回登录页', async ({ page, context }) => {
    await loginViaAPI(context);
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1500);

    const userDropdown = page.locator('.user-dropdown, .user-info').first();
    await expect(userDropdown).toBeVisible({ timeout: 5000 });
    await userDropdown.click();
    await page.waitForTimeout(500);

    const logoutBtn = page.locator('.el-dropdown-menu__item', { hasText: /退出|登出|Logout|Sign out/i }).first();
    await expect(logoutBtn).toBeVisible({ timeout: 3000 });
    await logoutBtn.click();

    await page.waitForURL('**/login**', { timeout: 10000 });
    expect(page.url()).toContain('/login');
  });

  test('9.2 登出后访问管理页面应重定向到登录页', async ({ page, context }) => {
    await loginViaAPI(context);
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const userDropdown = page.locator('.user-dropdown, .user-info').first();
    await userDropdown.click();
    await page.waitForTimeout(500);

    const logoutBtn = page.locator('.el-dropdown-menu__item', { hasText: /退出|登出|Logout|Sign out/i }).first();
    await logoutBtn.click();
    await page.waitForURL('**/login**', { timeout: 10000 });

    await page.goto('/#/admin/home');
    await page.waitForTimeout(2000);
    expect(page.url()).toContain('/login');
  });

  test('9.3 登出后登录页面应可正常渲染', async ({ page, context }) => {
    await loginViaAPI(context);
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const userDropdown = page.locator('.user-dropdown, .user-info').first();
    await userDropdown.click();
    await page.waitForTimeout(500);

    const logoutBtn = page.locator('.el-dropdown-menu__item', { hasText: /退出|登出|Logout|Sign out/i }).first();
    await logoutBtn.click();
    await page.waitForURL('**/login**', { timeout: 10000 });

    await expect(page.locator('.a-login-card')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('button', { hasText: /登录|Login/i })).toBeVisible();
  });
});
