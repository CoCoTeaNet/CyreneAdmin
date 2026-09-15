import { test, expect } from '@playwright/test';
import { loginViaAPI } from '../utils/auth';
import { API_URL } from '../utils/helpers';

/**
 * API 接口集成测试
 * 直接测试后端 API 在无前端参与时的行为
 */
test.describe('API 接口测试', () => {

  test('10.1 健康检查接口应返回 200', async ({ request }) => {
    const response = await request.get(`${API_URL}/system/dashboard/index`);
    expect(response.ok()).toBeTruthy();
  });

  test('10.2 未授权访问应返回 4001', async ({ request }) => {
    const response = await request.get(`${API_URL}/system/loginInfo`, {
      headers: { 'Authorization': '' },
    });
    const body = await response.json();
    expect(body.code).toBe(4001);
  });

  test('10.3 获取验证码接口应正常返回', async ({ request }) => {
    const response = await request.get(`${API_URL}/system/captcha?timestamp=${Date.now()}`);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
    expect(body.data.captchaId).toBeTruthy();
  });

  test('10.4 已授权访问 dashboard 数据应返回 200', async ({ request }) => {
    // 先登录获取 token
    const loginRes = await request.post(`${API_URL}/system/login`, {
      data: {
        username: 'admin',
        password: 'admin#123456',
        captcha: '0000',
        captchaId: 'test',
        publicKey: 'test',
      },
    });
    const loginBody = await loginRes.json();
    expect(loginBody.code).toBe(200);
    const token = loginBody.data;

    // 使用 token 获取数据
    const response = await request.get(`${API_URL}/system/dashboard/getCount`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('10.5 用户列表分页接口应正常工作', async ({ request }) => {
    const loginRes = await request.post(`${API_URL}/system/login`, {
      data: {
        username: 'admin',
        password: 'admin#123456',
        captcha: '0000',
        captchaId: 'test',
        publicKey: 'test',
      },
    });
    const token = (await loginRes.json()).data;

    const response = await request.post(`${API_URL}/system/user/listByPage`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: { pageNo: 1, pageSize: 10, sysUser: {} },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.records).toBeTruthy();
    expect(Array.isArray(body.data.records)).toBeTruthy();
  });

  test('10.6 角色列表分页接口应正常工作', async ({ request }) => {
    const loginRes = await request.post(`${API_URL}/system/login`, {
      data: {
        username: 'admin',
        password: 'admin#123456',
        captcha: '0000',
        captchaId: 'test',
        publicKey: 'test',
      },
    });
    const token = (await loginRes.json()).data;

    const response = await request.post(`${API_URL}/system/role/listByPage`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: { pageNo: 1, pageSize: 10, sysRole: {} },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data.records)).toBeTruthy();
  });

  test('10.7 菜单树接口应正常工作', async ({ request }) => {
    const loginRes = await request.post(`${API_URL}/system/login`, {
      data: {
        username: 'admin',
        password: 'admin#123456',
        captcha: '0000',
        captchaId: 'test',
        publicKey: 'test',
      },
    });
    const token = (await loginRes.json()).data;

    const response = await request.post(`${API_URL}/system/menu/listByTree`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: {},
    });
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('10.8 登出接口应正常工作', async ({ request }) => {
    const loginRes = await request.post(`${API_URL}/system/login`, {
      data: {
        username: 'admin',
        password: 'admin#123456',
        captcha: '0000',
        captchaId: 'test',
        publicKey: 'test',
      },
    });
    const token = (await loginRes.json()).data;

    const logoutRes = await request.post(`${API_URL}/system/logout`, {
      headers: { 'Authorization': token },
    });
    const body = await logoutRes.json();
    expect(body.code).toBe(200);
  });
});
