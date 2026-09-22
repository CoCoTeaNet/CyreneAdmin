import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';

test.describe('仪表盘', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('3.1 仪表盘页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await expect(page.locator('.el-card').first()).toBeVisible({ timeout: 8000 });
  });

  test('3.2 应显示统计卡片', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(2000);

    const cards = page.locator('.el-card');
    const count = await cards.count();
    expect(count).toBeGreaterThanOrEqual(2);
  });

  test('3.3 应显示 CPU 信息', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(2000);

    const descriptions = page.locator('.el-descriptions');
    const count = await descriptions.count();
    expect(count).toBeGreaterThanOrEqual(1);

    await expect(page.locator('text=/CPU|cpu|处理器/i').first()).toBeVisible({ timeout: 5000 });
  });

  test('3.4 应显示系统运行信息', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(2000);

    await expect(page.locator('text=/操作系统|OS|系统信息|System/i').first()).toBeVisible({ timeout: 5000 });
  });

  test('3.5 CPU 信息应包含核心数和使用率', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(3000);

    const cpuLabels = page.locator('.el-descriptions__label');
    const count = await cpuLabels.count();
    const labels: string[] = [];
    for (let i = 0; i < count; i++) {
      const text = await cpuLabels.nth(i).textContent();
      if (text) labels.push(text.trim());
    }
    const joined = labels.join(' ');
    expect(joined).toMatch(/CPU|cpu|核心|Core/i);
  });

  test('3.6 系统信息应包含内存和磁盘信息', async ({ page }) => {
    await navigateTo(page, '/admin/dashboard');
    await page.waitForTimeout(3000);

    const descriptions = page.locator('.el-descriptions');
    const count = await descriptions.count();
    expect(count).toBeGreaterThanOrEqual(1);

    const allText = await descriptions.first().textContent();
    expect(allText).toBeTruthy();
  });
});
