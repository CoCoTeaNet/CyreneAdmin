import { test, expect } from '@playwright/test';
import { loginViaAPI, navigateTo } from '../utils/auth';
import { waitForTableLoad } from '../utils/helpers';

test.describe('个人中心', () => {
  test.beforeEach(async ({ context }) => {
    await loginViaAPI(context);
  });

  test('11.1 个人中心页面应正确加载', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    await expect(page.locator('.el-card').first()).toBeVisible({ timeout: 8000 });
  });

  test('11.2 应显示个人信息描述列表', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    const descriptions = page.locator('.el-descriptions');
    await expect(descriptions.first()).toBeVisible({ timeout: 8000 });

    await expect(page.locator('.el-descriptions', { hasText: /admin/i }).first()).toBeVisible({ timeout: 5000 });
  });

  test('11.3 应显示用户名和角色信息', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    await expect(page.locator('text=admin').first()).toBeVisible({ timeout: 8000 });

    const roleTags = page.locator('.el-descriptions .el-tag');
    const count = await roleTags.count();
    expect(count).toBeGreaterThan(0);
  });

  test('11.4 应显示更新资料表单', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    const cards = page.locator('.el-card');
    const cardCount = await cards.count();
    expect(cardCount).toBeGreaterThanOrEqual(2);

    const secondCard = cards.nth(1);
    await expect(secondCard).toBeVisible();

    await expect(secondCard.locator('.el-form-item').filter({ hasText: /昵称|Nickname/i }).first()).toBeVisible();
    await expect(secondCard.locator('.el-form-item').filter({ hasText: /邮箱|Email/i }).first()).toBeVisible();
    await expect(secondCard.locator('.el-form-item').filter({ hasText: /手机|Mobile|电话/i }).first()).toBeVisible();
  });

  test('11.5 应显示修改密码表单', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    await expect(page.locator('text=/修改密码|Modify Password|Change Password/i').first()).toBeVisible({ timeout: 5000 });

    const pwdSection = page.locator('.el-form-item').filter({ hasText: /旧密码|Old Password|原密码/i });
    await expect(pwdSection.first()).toBeVisible();

    const newPwdSection = page.locator('.el-form-item').filter({ hasText: /新密码|New Password/i });
    await expect(newPwdSection.first()).toBeVisible();

    const confirmPwdSection = page.locator('.el-form-item').filter({ hasText: /确认|重复|Repeat|Confirm.*Password/i });
    await expect(confirmPwdSection.first()).toBeVisible();
  });

  test('11.6 应显示头像上传区域', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    const uploadArea = page.locator('.el-upload');
    await expect(uploadArea.first()).toBeVisible({ timeout: 5000 });
  });

  test('11.7 应能通过头部导航进入个人中心', async ({ page }) => {
    await navigateTo(page, '/admin/home');
    await page.waitForTimeout(1000);

    const userInfo = page.locator('.user-info, .user-dropdown').first();
    if (await userInfo.isVisible().catch(() => false)) {
      await userInfo.click();
      await page.waitForTimeout(500);

      const profileLink = page.locator('.el-dropdown-menu__item', { hasText: /个人中心|个人信息|Profile|User Center/i }).first();
      if (await profileLink.isVisible().catch(() => false)) {
        await profileLink.click();
        await page.waitForTimeout(2000);
        expect(page.url()).toContain('/admin/sys-user-center');
      }
    }
  });

  test('11.8 更新资料按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    const updateBtn = page.locator('button', { hasText: /更新信息|修改|Update|Save/i }).first();
    await expect(updateBtn).toBeVisible({ timeout: 5000 });
  });

  test('11.9 修改密码按钮应存在', async ({ page }) => {
    await navigateTo(page, '/admin/sys-user-center');
    await page.waitForTimeout(2000);

    const modifyPwdBtn = page.locator('button', { hasText: /修改密码|Modify Password|Change Password/i });
    await expect(modifyPwdBtn.first()).toBeVisible({ timeout: 5000 });
  });
});
