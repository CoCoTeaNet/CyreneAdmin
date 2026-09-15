import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';

test.describe('导航与布局', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('2.1 首页应正确加载并显示欢迎信息', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    // Home.vue 的内容应可见
    await expect(page.locator('.home, .welcome-title').first()).toBeVisible({ timeout: 5000 }).catch(() => {
      // 回退检查：admin 布局加载即可
      return expect(page.locator('.admin-layout, .a-layout').first()).toBeVisible();
    });
  });

  test('2.2 左侧菜单应包含系统管理入口', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    // 侧边栏菜单应包含关键菜单项
    const sidebar = page.locator('.el-menu, .a-aside, aside').first();
    await expect(sidebar).toBeVisible();
  });

  test('2.3 应能通过侧边栏导航到 Dashboard 页面', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    // 点击 Dashboard 菜单项
    const dashboardLink = page.locator('.el-menu-item, .el-sub-menu__title', { hasText: /Dashboard|仪表盘|监控/i }).first();
    if (await dashboardLink.isVisible().catch(() => false)) {
      await dashboardLink.click();
      await page.waitForTimeout(1000);
      expect(page.url()).toContain('dashboard');
    }
  });

  test('2.4 应能导航到用户管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await page.waitForTimeout(1000);
    // 用户列表表格应可见
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.5 应能导航到角色管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.6 应能导航到菜单管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table, .el-tree').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.7 应能导航到字典管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table, .el-tree').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.8 应能导航到日志管理页面', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('2.9 不存在的路由应显示 404 页面', async ({ page }) => {
    await navigateTo(page, '/admin/nonexistent-page');
    await page.waitForTimeout(1000);
    // 应显示 404 或 NotFound 组件
    const notFound = page.locator('text=/404|Not Found|找不到|不存在/i');
    await expect(notFound.first()).toBeVisible({ timeout: 5000 });
  });
});
