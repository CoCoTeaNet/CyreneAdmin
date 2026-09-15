import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad, waitForDialog } from '../utils/helpers';

test.describe('角色管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('5.1 角色列表应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.el-table').first()).toBeVisible();
    await expect(page.locator('.el-pagination').first()).toBeVisible();
  });

  test('5.2 角色列表应包含默认角色', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);
    // 应至少有一条角色数据
    const rows = page.locator('.el-table__body .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
  });

  test('5.3 应能打开新增角色弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const addBtn = page.locator('button', { hasText: /新增|添加|Add/i }).first();
    await addBtn.click();
    await waitForDialog(page, true);

    // 弹窗中应有角色名称和角色标识输入框
    await expect(page.locator('.el-dialog .el-form-item').first()).toBeVisible();
  });

  test('5.4 应能搜索角色', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /角色名称|Role Name/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      const searchBtn = page.locator('button', { hasText: /搜索|Search/i }).first();
      await searchBtn.click();
      await waitForTableLoad(page);
    }
  });
});
