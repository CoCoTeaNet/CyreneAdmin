import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad } from '../utils/helpers';

test.describe('日志管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('8.1 日志列表应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.el-table').first()).toBeVisible();
    await expect(page.locator('.el-pagination').first()).toBeVisible();
  });

  test('8.2 日志列表应包含登录日志', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);
    // 至少应有一条日志记录（登录操作）
    const rows = page.locator('.el-table__body .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
  });
});
