import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad } from '../utils/helpers';

test.describe('菜单管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('6.1 菜单管理页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);
    // 菜单页面可能是表格或树形展示
    const content = page.locator('.el-table, .el-tree').first();
    await expect(content).toBeVisible({ timeout: 8000 });
  });

  test('6.2 应有新增菜单按钮', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);
    const addBtn = page.locator('button', { hasText: /新增|添加|Add/i }).first();
    await expect(addBtn).toBeVisible();
  });

  test('6.3 权限管理页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
    await waitForTableLoad(page, 10000);
    const content = page.locator('.el-table, .el-tree').first();
    await expect(content).toBeVisible({ timeout: 8000 });
  });
});
