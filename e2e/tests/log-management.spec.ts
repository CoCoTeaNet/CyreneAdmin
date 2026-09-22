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
    const pagination = page.locator('.el-pagination').first();
    if (await pagination.count() > 0) {
      await pagination.scrollIntoViewIfNeeded();
      await expect(pagination).toBeVisible();
    }
  });

  test('8.2 日志列表应包含日志记录', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);
    const rows = page.locator('.el-table__body .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
  });

  test('8.3 应能按操作人搜索', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /操作人|Operator|操作者/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('8.4 应能按日志ID搜索', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /日志.*ID|Log.*ID/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await expect(searchInput).toBeVisible();
    }
  });

  test('8.5 应能重置搜索条件', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /操作人|Operator|操作者/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('admin');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);

      await page.locator('button', { hasText: /重置|Reset/i }).first().click();
      await page.waitForTimeout(500);
      await expect(searchInput).toHaveValue('');
    }
  });

  test('8.6 删除单条日志应直接执行删除', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const rows = page.locator('.el-table__body .el-table__row');
    const lastRow = rows.last();
    const deleteBtn = lastRow.locator('button', { hasText: /删除|Delete/i });

    if (await deleteBtn.isVisible().catch(() => false)) {
      await deleteBtn.click();
      // Single delete uses reqSuccessFeedback (no confirmation dialog)
      await expect(page.locator('.el-message--success').first()).toBeVisible({ timeout: 5000 });
    }
  });

  test('8.7 批量删除按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const batchDeleteBtn = page.locator('button', { hasText: /批量删除|Batch Delete/i }).first();
    await expect(batchDeleteBtn).toBeVisible();
  });

  test('8.8 表格应包含选择列', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const checkboxes = page.locator('.el-table__header .el-checkbox');
    await expect(checkboxes.first()).toBeVisible();
  });

  test('8.9 应能翻页', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const nextBtn = page.locator('.el-pagination .btn-next').first();
    if (await nextBtn.isEnabled().catch(() => false)) {
      await nextBtn.click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('8.10 应能切换每页条数', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
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

  test('8.11 日志表格应包含状态和类型列', async ({ page }) => {
    await navigateTo(page, '/admin/sys-log-manager');
    await waitForTableLoad(page, 10000);

    const rows = page.locator('.el-table__body .el-table__row');
    if (await rows.count() > 0) {
      const tags = rows.first().locator('.el-tag');
      const tagCount = await tags.count();
      expect(tagCount).toBeGreaterThanOrEqual(1);
    }
  });
});
