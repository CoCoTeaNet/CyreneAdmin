import { test, expect, type APIRequestContext } from '@playwright/test';
import { apiLoginRequest, loginViaAPI, navigateTo } from '../utils/auth';
import { API_URL } from '../utils/config';
import { waitForTableLoad, waitForDialog, fillElInput } from '../utils/helpers';

/**
 * 字典管理（左树右子项主从布局）
 *
 * 通过 API 预置一棵测试字典树（E2E 前缀），测试结束后级联删除，
 * 不依赖数据库中已有的业务数据。
 */

const SEED_ROOT = 'E2E测试字典';
const SEED_CHILD_A = 'E2E子项A';
const SEED_CHILD_B = 'E2E子项B';
const SEED_GRANDCHILD = 'E2E孙项A1';

let token = '';
let rootId = '';

async function apiAdd(request: APIRequestContext, data: Record<string, any>) {
  const res = await request.post(`${API_URL}/system/dictionary/add`, {
    headers: { Authorization: token },
    data,
  });
  const body = await res.json();
  if (body.code !== 200) {
    throw new Error(`新增字典失败: ${JSON.stringify(body)}`);
  }
}

async function findByName(request: APIRequestContext, name: string): Promise<any> {
  const res = await request.post(`${API_URL}/system/dictionary/listByTree`, {
    headers: { Authorization: token },
    data: {},
  });
  const body = await res.json();
  const walk = (nodes: any[]): any => {
    for (const n of nodes || []) {
      if (n.dictionaryName === name) return n;
      const found = walk(n.children || []);
      if (found) return found;
    }
    return null;
  };
  const node = walk(body.data || []);
  if (!node) throw new Error(`未找到字典: ${name}`);
  return node;
}

/** 左侧树节点（按名称定位） */
function treeNode(page: any, name: string) {
  return page.locator('.dm-tree .el-tree-node__content').filter({ hasText: name });
}

/** 右侧表格行（按名称定位） */
function tableRow(page: any, name: string) {
  return page.locator('.dm-table-wrap .el-table__body .el-table__row').filter({ hasText: name });
}

test.describe('字典管理', () => {
  test.beforeAll(async ({ request }) => {
    token = await apiLoginRequest(request);
    await apiAdd(request, { dictionaryName: SEED_ROOT, enableStatus: 0, sort: 900, parentId: '0' });
    const root = await findByName(request, SEED_ROOT);
    rootId = String(root.id);
    await apiAdd(request, { dictionaryName: SEED_CHILD_A, enableStatus: 0, sort: 10, parentId: rootId });
    const childA = await findByName(request, SEED_CHILD_A);
    await apiAdd(request, { dictionaryName: SEED_GRANDCHILD, enableStatus: 0, sort: 1, parentId: String(childA.id) });
    await apiAdd(request, { dictionaryName: SEED_CHILD_B, enableStatus: 1, sort: 20, parentId: rootId });
  });

  test.afterAll(async ({ request }) => {
    if (rootId) {
      await request.post(`${API_URL}/system/dictionary/deleteBatch`, {
        headers: { Authorization: token },
        data: [rootId],
      });
    }
  });

  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('7.1 应加载左树右子项布局', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);
    await expect(page.locator('.dm-tree-panel')).toBeVisible({ timeout: 8000 });
    await expect(page.locator('.dm-detail-panel')).toBeVisible();
    await expect(treeNode(page, '全部字典')).toBeVisible();
    await expect(treeNode(page, SEED_ROOT)).toBeVisible();
  });

  test('7.2 应有添加字典按钮并能打开弹窗', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const addBtn = page.locator('.dm-detail-actions button', { hasText: /添加字典|Add Dictionary/i }).first();
    await expect(addBtn).toBeVisible();
    await addBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /字典名称|Dictionary Name/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /备注|Remark/i })).toBeVisible();
    await expect(dialog.locator('.el-form-item').filter({ hasText: /是否启用|Enabled/i })).toBeVisible();

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('7.3 应能编辑字典', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const editBtn = tableRow(page, SEED_ROOT).locator('button', { hasText: /编辑|Edit/i });
    await expect(editBtn).toBeVisible();
    await editBtn.click();
    await waitForDialog(page, true);

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('input').first()).toHaveValue(SEED_ROOT);

    await dialog.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await waitForDialog(page, false);
  });

  test('7.4 删除字典应弹出确认框', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const deleteBtn = tableRow(page, SEED_ROOT).locator('button', { hasText: /删除|Delete/i });
    await expect(deleteBtn).toBeVisible();
    await deleteBtn.click();

    const confirmBox = page.locator('.el-message-box');
    await expect(confirmBox).toBeVisible({ timeout: 5000 });
    // 含子项的字典应提示子项将一并删除
    await expect(confirmBox).toContainText(/子项|sub-items/i);

    await confirmBox.locator('button', { hasText: /取消|Cancel/i }).first().click();
    await expect(confirmBox).toBeHidden({ timeout: 5000 });
  });

  test('7.5 应能按名称实时筛选树', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    await page.locator('.dm-filter input').first().fill(SEED_CHILD_A);
    await page.waitForTimeout(400);
    // 匹配节点及其祖先保留，无关节点被过滤
    await expect(treeNode(page, SEED_CHILD_A)).toBeVisible();
    await expect(treeNode(page, SEED_ROOT)).toBeVisible();
    await expect(treeNode(page, SEED_CHILD_B)).toHaveCount(0);
  });

  test('7.6 应能按启用状态筛选', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    // 选中种子根节点，右侧展示其直接子项
    await treeNode(page, SEED_ROOT).click();
    await expect(tableRow(page, SEED_CHILD_A)).toBeVisible({ timeout: 5000 });
    await expect(tableRow(page, SEED_CHILD_B)).toBeVisible();

    await page.locator('.dm-filter .el-select').click();
    const dropdown = page.locator('.el-select-dropdown').last();
    await expect(dropdown).toBeVisible({ timeout: 3000 });
    await dropdown.locator('.el-select-dropdown__item').filter({ hasText: /^关闭$|^Disable$/i }).click();
    await page.waitForTimeout(400);

    await expect(tableRow(page, SEED_CHILD_B)).toBeVisible();
    await expect(tableRow(page, SEED_CHILD_A)).toHaveCount(0);
  });

  test('7.7 应能重置筛选条件', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const searchInput = page.locator('.dm-filter input').first();
    await searchInput.fill(SEED_CHILD_A);
    await page.waitForTimeout(300);
    await page.locator('.dm-filter button', { hasText: /重置|Reset/i }).click();
    await page.waitForTimeout(300);
    await expect(searchInput).toHaveValue('');
    await expect(treeNode(page, SEED_CHILD_B)).toBeVisible();
  });

  test('7.8 应能展开/收起全部树节点', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    // 初始全部展开，孙节点可见
    await expect(treeNode(page, SEED_GRANDCHILD)).toBeVisible();

    const toggleBtn = page.locator('.dm-panel-tools button').first();
    await toggleBtn.click();
    await page.waitForTimeout(400);
    // 收起后节点仍保留在 DOM 中，仅隐藏
    await expect(treeNode(page, SEED_GRANDCHILD)).toBeHidden();

    await toggleBtn.click();
    await page.waitForTimeout(400);
    await expect(treeNode(page, SEED_GRANDCHILD)).toBeVisible();
  });

  test('7.9 批量删除按钮应存在且默认禁用', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const batchDeleteBtn = page.locator('.dm-batch-btn');
    await expect(batchDeleteBtn).toBeVisible();
    await expect(batchDeleteBtn).toBeDisabled();
  });

  test('7.10 勾选行后应启用批量删除', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    await expect(page.locator('.el-table__header .el-checkbox').first()).toBeVisible();
    await tableRow(page, SEED_ROOT).locator('.el-checkbox').click();
    await expect(page.locator('.dm-batch-btn')).toBeEnabled();
    await expect(page.locator('.dm-selected-info')).toContainText(/已选 1 项|1 selected/i);
  });

  test('7.11 点击树节点应切换右侧子项列表', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    await treeNode(page, SEED_ROOT).click();
    await expect(page.locator('.dm-detail-title h2')).toHaveText(SEED_ROOT, { timeout: 5000 });
    await expect(tableRow(page, SEED_CHILD_A)).toBeVisible();
    await expect(tableRow(page, SEED_CHILD_B)).toBeVisible();
    // 孙节点不属于直接子项，不应出现
    await expect(tableRow(page, SEED_GRANDCHILD)).toHaveCount(0);
    // 面包屑包含根与当前节点
    await expect(page.locator('.dm-breadcrumb')).toContainText('全部字典');
  });

  test('7.12 点击行名称应逐级下钻', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    await tableRow(page, SEED_ROOT).locator('.dm-name-link').click();
    await expect(page.locator('.dm-detail-title h2')).toHaveText(SEED_ROOT, { timeout: 5000 });

    await tableRow(page, SEED_CHILD_A).locator('.dm-name-link').click();
    await expect(page.locator('.dm-detail-title h2')).toHaveText(SEED_CHILD_A, { timeout: 5000 });
    await expect(tableRow(page, SEED_GRANDCHILD)).toBeVisible();
    await expect(page.locator('.dm-breadcrumb')).toContainText(SEED_ROOT);
  });

  test('7.13 应能完成新增-编辑-删除子项全流程', async ({ page }) => {
    await navigateTo(page, '/admin/sys-dictionary-manager');
    await waitForTableLoad(page, 10000);

    const tempName = 'E2E临时子项';

    // 新增：选中种子根节点后添加子项
    await treeNode(page, SEED_ROOT).click();
    await expect(page.locator('.dm-detail-title h2')).toHaveText(SEED_ROOT, { timeout: 5000 });
    await page.locator('.dm-detail-actions button', { hasText: /添加子项|Add Sub-item/i }).click();
    await waitForDialog(page, true);
    await fillElInput(page, '字典名称', tempName);
    await page.locator('.el-dialog button', { hasText: /确认|Confirm/i }).click();
    await waitForDialog(page, false);
    await waitForTableLoad(page);
    await expect(tableRow(page, tempName)).toBeVisible({ timeout: 8000 });

    // 编辑：修改备注
    await tableRow(page, tempName).locator('button', { hasText: /编辑|Edit/i }).click();
    await waitForDialog(page, true);
    await fillElInput(page, '备注', 'E2E备注已更新');
    await page.locator('.el-dialog button', { hasText: /确认|Confirm/i }).click();
    await waitForDialog(page, false);
    await waitForTableLoad(page);
    await expect(tableRow(page, tempName)).toContainText('E2E备注已更新', { timeout: 8000 });

    // 删除：确认后行消失
    await tableRow(page, tempName).locator('button', { hasText: /删除|Delete/i }).click();
    const confirmBox = page.locator('.el-message-box');
    await expect(confirmBox).toBeVisible({ timeout: 5000 });
    await confirmBox.locator('button', { hasText: /确认|Confirm/i }).click();
    await expect(tableRow(page, tempName)).toHaveCount(0, { timeout: 8000 });
  });
});
