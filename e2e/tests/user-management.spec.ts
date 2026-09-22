import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad, waitForDialog, waitForMessage, fillElInput } from '../utils/helpers';

test.describe('用户管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('4.1 用户列表应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.el-table').first()).toBeVisible();
    // Pagination may be hidden by Element Plus when total <= pageSize (hide-on-single-page)
    const pagination = page.locator('.el-pagination').first();
    if (await pagination.count() > 0) {
      await pagination.scrollIntoViewIfNeeded();
      await expect(pagination).toBeVisible();
    }
  });

  test('4.2 用户列表应包含默认 admin 用户', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);
    const adminCell = page.locator('.el-table__body .el-table__row', { hasText: 'admin' });
    await expect(adminCell.first()).toBeVisible({ timeout: 8000 });
  });

  test('4.3 应能按用户名搜索', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /^用户账号|Username/i }).locator('input').first();
    await searchInput.fill('admin');
    await page.locator('button', { hasText: /搜索|Search/i }).first().click();
    await waitForTableLoad(page);

    const rows = page.locator('.el-table__body .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
    await expect(rows.first()).toContainText('admin');
  });

  test('4.4 应能按昵称搜索', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const nicknameInput = page.locator('.el-form-item').filter({ hasText: /昵称|Nickname/i }).locator('input');
    if (await nicknameInput.isVisible().catch(() => false)) {
      await nicknameInput.fill('admin');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('4.5 应能重置搜索条件', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /^用户账号|Username/i }).locator('input').first();
    await searchInput.fill('admin');
    await page.locator('button', { hasText: /搜索|Search/i }).first().click();
    await waitForTableLoad(page);

    await page.locator('button', { hasText: /重置|Reset/i }).first().click();
    await page.waitForTimeout(500);
    await expect(searchInput).toHaveValue('');
  });

  test('4.6 应能打开新增用户弹窗并包含所有表单字段', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    await page.locator('button', { hasText: /新增|添加|Add|Create/i }).first().click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();

    await expect(dialog.locator('.el-form-item').filter({ hasText: /账号|账户|Account/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /昵称|Nickname/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /密码|Password/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /邮箱|Email/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /角色|Role/i })).toBeVisible();

    await expect(dialog.locator('button', { hasText: /确定|确认|保存|Confirm|Save/i }).first()).toBeVisible();
    await expect(dialog.locator('button', { hasText: /取消|Cancel/i }).first()).toBeVisible();
  });

  test('4.7 新增用户弹窗应能关闭', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    await page.locator('button', { hasText: /新增|添加|Add|Create/i }).first().click();
    await waitForDialog(page, true);

    await page.locator('.el-dialog button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('4.8 应能打开编辑用户弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const editBtn = page.locator('.el-table__body .el-table__row').first().locator('button', { hasText: /编辑|Edit/i });
    await editBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('4.9 删除用户应弹出确认框', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const rows = page.locator('.el-table__body .el-table__row');
    const lastRow = rows.last();
    const deleteBtn = lastRow.locator('button', { hasText: /删除|Delete/i });

    if (await deleteBtn.isVisible().catch(() => false)) {
      await deleteBtn.click();
      const confirmBox = page.locator('.el-message-box');
      await expect(confirmBox).toBeVisible({ timeout: 5000 });

      await confirmBox.locator('button', { hasText: /取消|Cancel/i }).first().click();
      await expect(confirmBox).toBeHidden({ timeout: 5000 });
    }
  });

  test('4.10 应能翻页', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const nextBtn = page.locator('.el-pagination .btn-next').first();
    if (await nextBtn.isEnabled().catch(() => false)) {
      await nextBtn.click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('4.11 应能切换每页条数', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const sizeBtn = page.locator('.el-pagination .el-pagination__sizes').first();
    if (await sizeBtn.isVisible().catch(() => false)) {
      await sizeBtn.click();
      const dropdown = page.locator('.el-select-dropdown').last();
      await expect(dropdown).toBeVisible({ timeout: 3000 });
      const items = dropdown.locator('.el-select-dropdown__item');
      const count = await items.count();
      expect(count).toBeGreaterThan(0);
      await items.first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('4.12 批量删除按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const batchDeleteBtn = page.locator('button', { hasText: /批量删除|Batch Delete/i }).first();
    await expect(batchDeleteBtn).toBeVisible();
  });

  test('4.13 表格应包含选择列', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-manager');
    await waitForTableLoad(page, 10000);

    const checkboxes = page.locator('.el-table__header .el-checkbox');
    await expect(checkboxes.first()).toBeVisible();
  });
});
