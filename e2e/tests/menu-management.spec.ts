import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad, waitForDialog } from '../utils/helpers';

test.describe('菜单管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('6.1 菜单管理页面应正确加载并显示树形表格', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });

    const rows = page.locator('.el-table__body .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
  });

  test('6.2 应有新增菜单按钮并能打开弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);

    const addBtn = page.locator('button', { hasText: /新增菜单|添加菜单|Add Menu/i }).first();
    await expect(addBtn).toBeVisible();
    await addBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /菜单名称|Menu Name/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /菜单类型|Menu Type/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /路由地址|路由路径|Router/i })).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('6.3 应能编辑菜单（打开编辑弹窗并关闭）', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);

    const editBtn = page.locator('.el-table__body .el-table__row').first().locator('button', { hasText: /编辑|Edit/i });
    await editBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('6.4 删除菜单应弹出确认框', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
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

  test('6.5 应能搜索菜单', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /菜单名称|Menu Name/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('系统');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('6.6 应能重置搜索条件', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /菜单名称|Menu Name/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('系统');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);

      await page.locator('button', { hasText: /重置|Reset/i }).first().click();
      await page.waitForTimeout(500);
      await expect(searchInput).toHaveValue('');
    }
  });

  test('6.7 应有展开/收起全部按钮', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);

    const expandBtn = page.locator('button', { hasText: /展开|收起|Expand|Collapse/i }).first();
    await expect(expandBtn).toBeVisible();
    await expandBtn.click();
    await page.waitForTimeout(500);
    await expect(page.locator('.el-table').first()).toBeVisible();
  });

  test('6.8 菜单表格应包含类型和状态列', async ({ page }) => {
    await navigateTo(page, '/admin/sys-menu-manager');
    await waitForTableLoad(page, 10000);

    const headers = page.locator('.el-table__header-wrapper th');
    const headerTexts: string[] = [];
    const count = await headers.count();
    for (let i = 0; i < count; i++) {
      const text = await headers.nth(i).textContent();
      if (text) headerTexts.push(text.trim());
    }
    const joined = headerTexts.join(' ');
    expect(joined).toMatch(/菜单类型|类型|Menu Type/i);
  });
});

test.describe('权限管理', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('6.9 权限管理页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 });
  });

  test('6.10 应有新增权限按钮并能打开弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
    await waitForTableLoad(page, 10000);

    const addBtn = page.locator('button', { hasText: /新增权限|添加权限|Add Permission/i }).first();
    await expect(addBtn).toBeVisible();
    await addBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /权限名称|Permission Name/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /权限编号|权限标识|Permission Code/i })).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('6.11 应能编辑权限', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
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

  test('6.12 删除权限应弹出确认框', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
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

  test('6.13 应能搜索权限', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.el-form-item').filter({ hasText: /权限名称|Permission Name/i }).locator('input');
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill('系统');
      await page.locator('button', { hasText: /搜索|Search/i }).first().click();
      await waitForTableLoad(page);
      await expect(page.locator('.el-table').first()).toBeVisible();
    }
  });

  test('6.14 批量删除按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-permission-manager');
    await waitForTableLoad(page, 10000);

    const batchDeleteBtn = page.locator('button', { hasText: /批量删除|Batch Delete/i }).first();
    await expect(batchDeleteBtn).toBeVisible();
  });
});
