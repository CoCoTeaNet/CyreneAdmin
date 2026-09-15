import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad, waitForDialog, waitForMessage } from '../utils/helpers';

test.describe('用户管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('4.1 用户列表应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);
    // 表格应可见
    await expect(page.locator('.el-table').first()).toBeVisible();
    // 分页应可见
    await expect(page.locator('.el-pagination').first()).toBeVisible();
  });

  test('4.2 用户列表应包含默认 admin 用户', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);
    // 表格行中应包含 admin 用户名
    const adminCell = page.locator('.el-table__body .el-table__row', { hasText: 'admin' });
    await expect(adminCell.first()).toBeVisible({ timeout: 8000 });
  });

  test('4.3 应能搜索用户', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    // 在用户名搜索框中输入
    const searchInput = page.locator('.el-form-item').filter({ hasText: /用户名|Username|账号/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      // 点击搜索按钮
      const searchBtn = page.locator('button', { hasText: /搜索|Search/i }).first();
      await searchBtn.click();
      await waitForTableLoad(page);
      // 结果应包含 admin
      await expect(page.locator('.el-table__body .el-table__row', { hasText: 'admin' }).first()).toBeVisible();
    }
  });

  test('4.4 应能打开新增用户弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    // 点击新增按钮
    const addBtn = page.locator('button', { hasText: /新增|添加|Add|Create/i }).first();
    await addBtn.click();
    await waitForDialog(page, true);

    // 弹窗中应有保存/确认按钮
    await expect(page.locator('.el-dialog button', { hasText: /确定|保存|Confirm|Save|Submit/i }).first()).toBeVisible();
  });

  test('4.5 应能编辑用户（打开编辑弹窗并关闭）', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    // 点击第一行的编辑按钮
    const editBtn = page.locator('.el-table__body .el-table__row').first().locator('button', { hasText: /编辑|Edit/i });
    if (await editBtn.isVisible().catch(() => false)) {
      await editBtn.click();
      await waitForDialog(page, true);
      // 关闭弹窗
      const cancelBtn = page.locator('.el-dialog button', { hasText: /取消|Cancel/i }).first();
      await cancelBtn.click();
      await waitForDialog(page, false);
    }
  });

  test('4.6 应能翻页', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    // 查找下一页按钮
    const nextBtn = page.locator('.el-pagination .btn-next').first();
    if (await nextBtn.isEnabled().catch(() => false)) {
      await nextBtn.click();
      await waitForTableLoad(page);
      // 翻页后表格仍应可见
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });
});
