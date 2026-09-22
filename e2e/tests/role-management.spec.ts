import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad, waitForDialog, fillElInput } from '../utils/helpers';

test.describe('角色管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('5.1 角色列表应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.el-table').first()).toBeVisible();
    const pagination = page.locator('.el-pagination').first();
    if (await pagination.count() > 0) {
      await pagination.scrollIntoViewIfNeeded();
      await expect(pagination).toBeVisible();
    }
  });

  test('5.2 角色列表应包含默认角色', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);
    const rows = page.locator('.el-table__body .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
  });

  test('5.3 应能按角色名称搜索', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /角色名称|Role Name/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('5.4 应能按角色标识搜索', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /角色标识|Role Key/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('5.5 应能重置搜索条件', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /角色名称|Role Name/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);

      await page.locator('button', { hasText: /重置|Reset/i }).first().click();
      await page.waitForTimeout(500);
      await expect(searchInput).toHaveValue('');
    }
  });

  test('5.6 应能打开新增角色弹窗并包含表单字段', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    await page.locator('button', { hasText: /新增|添加|Add/i }).first().click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /角色名称|Role Name/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /角色标识|Role Key/i })).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('5.7 应能打开授予菜单弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const assignMenuBtn = page.locator('.el-table__body .el-table__row').first()
      .locator('button', { hasText: /分配菜单|Assign Menu/i });
    if (await assignMenuBtn.isVisible().catch(() => false)) {
      await assignMenuBtn.click();
      const dialog = page.locator('.el-dialog');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      await expect(dialog.locator('.el-tree').first()).toBeVisible({ timeout: 5000 });
      await expect(dialog.locator('button', { hasText: /确定|Confirm/i }).first()).toBeVisible();
      await expect(dialog.locator('button', { hasText: /取消|Cancel/i }).first()).toBeVisible();

      await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
      await expect(dialog).toBeHidden({ timeout: 5000 });
    }
  });

  test('5.8 应能打开授予权限弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const assignPermBtn = page.locator('.el-table__body .el-table__row').first()
      .locator('button', { hasText: /分配权限|Assign Permission/i });
    if (await assignPermBtn.isVisible().catch(() => false)) {
      await assignPermBtn.click();
      const dialog = page.locator('.el-dialog');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      await expect(dialog.locator('.el-tree').first()).toBeVisible({ timeout: 5000 });

      await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
      await expect(dialog).toBeHidden({ timeout: 5000 });
    }
  });

  test('5.9 批量删除按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const batchDeleteBtn = page.locator('button', { hasText: /批量删除|Batch Delete/i }).first();
    await expect(batchDeleteBtn).toBeVisible();
  });

  test('5.10 表格应包含选择列', async ({ page }) => {
    await navigateTo(page, '/admin/sys-role-manager');
    await waitForTableLoad(page, 10000);

    const checkboxes = page.locator('.el-table__header .el-checkbox');
    await expect(checkboxes.first()).toBeVisible();
  });
});
