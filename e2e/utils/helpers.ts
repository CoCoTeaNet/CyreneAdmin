import { type Page, type Locator, expect } from '@playwright/test';
import { TEST_PASSWORD } from './config';

/** 后端 API 基础路径 */
export const API_URL = process.env.API_URL || 'http://localhost:9000/api';

/** 默认测试账号 */
export const TEST_USER = {
  username: 'admin',
  password: TEST_PASSWORD,
};

/**
 * 等待并确认 Element Plus 消息提示出现
 */
export async function waitForMessage(page: Page, text: string, timeout = 5000): Promise<void> {
  const msg = page.locator('.el-message', { hasText: text });
  await expect(msg).toBeVisible({ timeout });
}

/**
 * 等待 Element Plus loading 遮罩消失
 */
export async function waitForTableLoad(page: Page, timeout = 10000): Promise<void> {
  await page.locator('.el-loading-mask').waitFor({ state: 'hidden', timeout }).catch(() => {});
  await page.waitForTimeout(300);
}

/**
 * 等待弹窗出现并确认可见
 */
export async function waitForDialog(page: Page, visible = true): Promise<void> {
  const dialog = page.locator('.el-dialog');
  if (visible) {
    await expect(dialog).toBeVisible({ timeout: 5000 });
  } else {
    await expect(dialog).toBeHidden({ timeout: 5000 });
  }
}

/**
 * 在 Element Plus 分页中确认总条数
 */
export async function expectPaginationTotal(page: Page, min = 0): Promise<void> {
  const total = page.locator('.el-pagination .el-pagination__total');
  await expect(total).toBeVisible();
  if (min > 0) {
    const text = await total.textContent();
    const match = text?.match(/(\d+)/);
    if (match) {
      expect(Number(match[1])).toBeGreaterThanOrEqual(min);
    }
  }
}

/**
 * 在表单对话框中填写 Element Plus 输入框
 */
export async function fillElInput(page: Page, label: string, value: string): Promise<void> {
  const formItem = page.locator(`.el-form-item`, { hasText: label }).first();
  const input = formItem.locator('.el-input__inner, input[type="text"], input[type="password"]').first();
  await input.clear();
  await input.fill(value);
}

/**
 * 在 Element Plus 下拉选择器中选择选项
 */
export async function selectElOption(page: Page, placeholder: string, optionText: string): Promise<void> {
  const select = page.locator('.el-select', { hasText: placeholder }).first();
  await select.click();
  const dropdown = page.locator('.el-select-dropdown');
  await expect(dropdown.first()).toBeVisible();
  const option = page.locator('.el-select-dropdown__item', { hasText: optionText });
  await option.click();
}

/**
 * 调用后端 API 创建用户（通过预置验证码绕过）
 * 后端 createUser 接口要求 captchaVerification，通过预置 Redis key 绕过
 */
export async function apiCreateUser(
  request: any,
  userData: Record<string, any>,
): Promise<any> {
  const res = await request.post(`${API_URL}/user/create`, {
    data: {
      ...userData,
      captchaVerification: JSON.stringify({ uuid: 'e2e-test-captcha', value: '1' }),
    },
  });
  return res.json();
}
