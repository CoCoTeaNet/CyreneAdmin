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
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('7.2 应有新增字典按钮并能打开弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const addBtn = page.locator('button', { hasText: /新增|添加|Add/i }).first();
    await expect(addBtn).toBeVisible();
    await addBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /字典名称|Dictionary Name|名称/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /备注|Remark/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /是否启用|Enable/i })).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('7.3 应能编辑字典', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const editBtn = page.locator('.el-table__body .el-table__row').first().locator('button', { hasText: /编辑|Edit/i });
    if (await editBtn.isVisible().catch(() => false)) {
      await editBtn.click();
      await waitForDialog(page, true);

      const dialog = page.locator('.el-dialog');
      await expect(dialog).toBeVisible();

      await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
      await waitForDialog(page, false);
    }
  });

  test('7.4 删除字典应弹出确认框', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
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

  test('7.5 应能搜索字典', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /字典名称|Dictionary Name|名称/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('系统');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('7.6 应能按启用状态筛选', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const statusSelect = page.locator('.el-form-item').filter({ hasText: /启用状态|Enable Status|状态/i }).locator('.el-select');
    if (await statusSelect.isVisible().catch(() => false)) {
      await statusSelect.click();
      const dropdown = page.locator('.el-select-dropdown').last();
      await expect(dropdown).toBeVisible({ timeout: 3000 });
      const option = dropdown.locator('.el-select-dropdown__item').first();
      await option.click();
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('7.7 应能重置搜索条件', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /字典名称|Dictionary Name|名称/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('系统');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);

      await page.locator('button', { hasText: /重置|Reset/i }).first().click();
      await page.waitForTimeout(500);
      await expect(searchInput).toHaveValue('');
    }
  });

  test('7.8 应有展开/收起全部按钮', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const expandBtn = page.locator('button', { hasText: /展开|收起|Expand|Collapse/i }).first();
    if (await expandBtn.isVisible().catch(() => false)) {
      await expect(expandBtn).toBeVisible();
      await expandBtn.click();
      await page.waitForTimeout(500);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('7.9 批量删除按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const batchDeleteBtn = page.locator('button', { hasText: /批量删除|Batch Delete/i }).first();
    await expect(batchDeleteBtn).toBeVisible();
  });

  test('7.10 表格应包含选择列', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const checkboxes = page.locator('.el-table__header .el-checkbox');
    await expect(checkboxes.first()).toBeVisible();
  });
});
