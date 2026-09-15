import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad } from '../utils/helpers';

test.describe('仪表盘', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
    await context.newPage().then(p => navigateTo(p, '/admin/dashboard'));
  });

  test('3.1 仪表盘页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    // Dashboard 包含统计卡片和系统信息
    await expect(page.locator('.el-card').first()).toBeVisible({ timeout: 8000 });
  });

  test('3.2 应显示 CPU 信息', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(2000);
    // Dashboard 中有 CPU 相关的描述信息
    const cpuSection = page.locator('.el-descriptions').first();
    await expect(cpuSection).toBeVisible({ timeout: 8000 });
  });

  test('3.3 应显示系统运行信息', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(2000);
    // 应至少有一个 el-descriptions 块（CPU 或 System Info）
    const descriptions = page.locator('.el-descriptions');
    const count = await descriptions.count();
    expect(count).toBeGreaterThanOrEqual(1);
  });
});
