import { test, expect, type Page } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import fs from 'fs';
import path from 'path';

const SHOT_DIR = path.join(__dirname, '..', 'test-results', 'ui-shots');

// 真实用户流：点击侧边栏叶子菜单才会经 ChildMenu.addTabItem 新增标签；
// 仅靠 hash 导航不会加标签（afterEach 只改标题，AdminTab 不重新挂载）。
async function clickMenu(page: Page, re: RegExp) {
  const item = page.locator('.el-menu-item', { hasText: re }).first();
  await item.click();
  await page.waitForTimeout(900);
}

const DASHBOARD_MENU = /Service Monitoring|服务监控/i;
const HOME_MENU = /System Home|系统首页/i;

test.describe('标签页右键菜单', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('菜单渲染/关闭/关闭其他标签', async ({ page }) => {
    const errors: string[] = [];
    page.on('console', (m) => {
      if (m.type() === 'error') {
        const txt = m.text();
        if (!/getAvatar|avatar|favicon|Failed to load resource/i.test(txt)) errors.push(txt);
      }
    });
    page.on('pageerror', (e) => errors.push(String(e)));

    await navigateTo(page, '/admin/home');
    await clickMenu(page, DASHBOARD_MENU);
    expect(page.url()).toContain('#/admin/dashboard');
    expect(await page.locator('.tab-item').count()).toBeGreaterThanOrEqual(2);

    // 右键最后一个标签（dashboard），菜单应含五项
    await page.locator('.tab-item').last().click({ button: 'right' });
    const menu = page.locator('.tab-ctx-menu');
    await expect(menu).toBeVisible();
    for (const label of ['刷新', '关闭', '关闭其他标签', '最大化', '新窗口打开']) {
      await expect(menu.getByText(label, { exact: true })).toBeVisible();
    }
    fs.mkdirSync(SHOT_DIR, { recursive: true });
    await page.screenshot({ path: path.join(SHOT_DIR, 'tab-ctx-menu.png') });

    // Esc 关闭
    await page.keyboard.press('Escape');
    await expect(menu).toBeHidden();

    // 关闭第一个标签（home）→ 仅剩 dashboard
    await page.locator('.tab-item').first().click({ button: 'right' });
    await page.locator('.tab-ctx-menu li', { hasText: '关闭' }).first().click();
    await page.waitForTimeout(900);
    expect(await page.locator('.tab-item').count()).toBe(1);

    // 再点首页菜单凑两个标签 → 关闭其他标签，仅保留 home
    await clickMenu(page, HOME_MENU);
    expect(await page.locator('.tab-item').count()).toBeGreaterThanOrEqual(2);
    await page.locator('.tab-item').last().click({ button: 'right' });
    await page.locator('.tab-ctx-menu li', { hasText: '关闭其他标签' }).click();
    await page.waitForTimeout(900);
    expect(await page.locator('.tab-item').count()).toBe(1);
    expect(page.url()).toContain('#/admin/home');

    expect(errors).toEqual([]);
  });

  test('最大化/退出/新窗口打开/刷新', async ({ page, context }) => {
    const errors: string[] = [];
    page.on('console', (m) => {
      if (m.type() === 'error') {
        const txt = m.text();
        if (!/getAvatar|avatar|favicon|Failed to load resource/i.test(txt)) errors.push(txt);
      }
    });
    page.on('pageerror', (e) => errors.push(String(e)));

    await navigateTo(page, '/admin/home');
    await clickMenu(page, DASHBOARD_MENU);
    expect(page.url()).toContain('#/admin/dashboard');

    // 最大化：侧边栏与头部操作区隐藏，标签栏保留
    await page.locator('.tab-item').last().click({ button: 'right' });
    await page.locator('.tab-ctx-menu li', { hasText: '最大化' }).click();
    await page.waitForTimeout(600);
    await expect(page.locator('.admin-aside')).toBeHidden();
    await expect(page.locator('.header-right')).toBeHidden();
    await expect(page.locator('.tabs-wrapper')).toBeVisible();
    fs.mkdirSync(SHOT_DIR, { recursive: true });
    await page.screenshot({ path: path.join(SHOT_DIR, 'tab-maximized.png') });

    // 退出最大化
    await page.locator('.tab-item').last().click({ button: 'right' });
    await page.locator('.tab-ctx-menu li', { hasText: '退出最大化' }).click();
    await page.waitForTimeout(600);
    await expect(page.locator('.admin-aside')).toBeVisible();

    // 新窗口打开：弹出页应落在该标签路由
    await page.locator('.tab-item').last().click({ button: 'right' });
    const [popup] = await Promise.all([
      context.waitForEvent('page'),
      page.locator('.tab-ctx-menu li', { hasText: '新窗口打开' }).click(),
    ]);
    await popup.waitForLoadState('domcontentloaded');
    await popup.waitForTimeout(1200);
    expect(popup.url()).toContain('#/admin/dashboard');
    await popup.close();

    // 刷新：重载后仍停留在该标签
    await page.locator('.tab-item').last().click({ button: 'right' });
    await page.locator('.tab-ctx-menu li', { hasText: '刷新' }).first().click();
    await page.waitForTimeout(2500);
    expect(page.url()).toContain('#/admin/dashboard');
    expect(await page.locator('.tab-item').count()).toBeGreaterThanOrEqual(1);

    expect(errors).toEqual([]);
  });
});