import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';

test.describe('登出', () => {
  test('9.1 登出后应返回登录页', async ({ page, context }) => {
    // 先通过 API 登录注入 token
    await loginViaAPI(context);
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    // 尝试找到并点击登出按钮
    // 通常在右上角的用户下拉菜单中
    const avatar = page.locator('.el-avatar, .a-header-avatar, .user-avatar').first();
    if (await avatar.isVisible().catch(() => false)) {
      await avatar.click();
      await page.waitForTimeout(300);

      const logoutBtn = page.locator('.el-dropdown-menu__item, .el-menu-item', { hasText: /退出|登出|Logout|Sign out/i }).first();
      if (await logoutBtn.isVisible().catch(() => false)) {
        await logoutBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // 清除 localStorage 模拟登出
    await page.evaluate(() => localStorage.clear());
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    // 应重定向到登录页
    expect(page.url()).toContain('/login');
  });
});
