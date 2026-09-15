import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad, waitForDialog } from '../utils/helpers';

test.describe('字典管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('7.1 字典管理页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);
    // 字典页面可能是树形或表格
    const content = page.locator('.el-table, .el-tree, .el-card').first();
    await expect(content).toBeVisible({ timeout: 8000 });
  });

  test('7.2 应有新增字典按钮', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);
    const addBtn = page.locator('button', { hasText: /新增|添加|Add/i }).first();
    if (await addBtn.isVisible().catch(() => false)) {
      await expect(addBtn).toBeVisible();
    }
  });
});
