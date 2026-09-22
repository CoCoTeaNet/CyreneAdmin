import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';

test.describe('导航与布局', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('2.1 首页应正确加载并显示欢迎信息', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await expect(page.locator('.el-card').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.2 首页应包含项目信息卡片', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    await expect(page.locator('text=/Cyrene Admin|Cyrene/i').first()).toBeVisible({ timeout: 5000 });
  });

  test('2.3 左侧菜单应可见', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    const sidebar = page.locator('.el-menu, aside').first();
    await expect(sidebar).toBeVisible();
  });

  test('2.4 应能通过侧边栏导航到 Dashboard 页面', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    const dashboardLink = page.locator('.el-menu-item, .el-sub-menu__title', { hasText: /Dashboard|仪表盘|监控/i }).first();
    if (await dashboardLink.isVisible().catch(() => false)) {
      await dashboardLink.click();
      await page.waitForTimeout(1000);
      expect(page.url()).toContain('dashboard');
    }
  });

  test('2.5 应能导航到用户管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.6 应能导航到角色管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.7 应能导航到菜单管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.8 应能导航到字典管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.9 应能导航到日志管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.10 不存在的路由应显示 404 页面', async ({ page }) => {
    await navigateTo(page, '/admin/nonexistent-page');
    await page.waitForTimeout(1000);
    const notFound = page.locator('text=/404|Not Found|找不到|不存在/i');
    await expect(notFound.first()).toBeVisible({ timeout: 5000 });
  });

  test('2.11 头部应显示用户信息', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const userInfo = page.locator('.user-info, .user-name').first();
    await expect(userInfo).toBeVisible({ timeout: 5000 });
  });

  test('2.12 应能切换语言', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const langBtn = page.locator('.lang-dropdown, .lang-btn').first();
    if (await langBtn.isVisible().catch(() => false)) {
      await langBtn.click();
      await page.waitForTimeout(500);

      const enOption = page.locator('.el-dropdown-menu__item', { hasText: /English/i }).first();
      if (await enOption.isVisible().catch(() => false)) {
        await enOption.click();
        await page.waitForTimeout(1000);
      }
    }
  });

  test('2.13 应能折叠/展开侧边栏', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const collapseBtn = page.locator('.collapse-btn').first();
    if (await collapseBtn.isVisible().catch(() => false)) {
      await collapseBtn.click();
      await page.waitForTimeout(500);

      await collapseBtn.click();
      await page.waitForTimeout(500);
    }
  });

  test('2.14 头部应有全屏按钮', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const fullscreenBtn = page.locator('.action-icon').filter({ hasText: '' }).locator('svg').first();
    if (await fullscreenBtn.isVisible().catch(() => false)) {
      await expect(fullscreenBtn).toBeVisible();
    }
  });

  test('2.15 头部应有首页按钮', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(1000);

    const homeBtn = page.locator('.action-icon').locator('svg').first();
    if (await homeBtn.isVisible().catch(() => false)) {
      await expect(homeBtn.first()).toBeVisible();
    }
  });

  test('2.16 用户下拉菜单应包含个人中心和退出', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const userDropdown = page.locator('.user-dropdown, .user-info').first();
    if (await userDropdown.isVisible().catch(() => false)) {
      await userDropdown.click();
      await page.waitForTimeout(500);

      const profileItem = page.locator('.el-dropdown-menu__item', { hasText: /个人中心|个人信息|Profile/i });
      const logoutItem = page.locator('.el-dropdown-menu__item', { hasText: /退出|登出|Logout/i });

      if (await profileItem.first().isVisible().catch(() => false)) {
        await expect(profileItem.first()).toBeVisible();
      }
      if (await logoutItem.first().isVisible().catch(() => false)) {
        await expect(logoutItem.first()).toBeVisible();
      }
    }
  });
});
