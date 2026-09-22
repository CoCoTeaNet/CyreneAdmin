import { test, expect } from '@playwright/test';
import { apiLoginRequest } from '../utils/auth';
import { API_URL, TEST_USER } from '../utils/helpers';

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
    expect(body.data.publicKey).toBeTruthy();
  });

  test('10.4 已授权访问 dashboard 统计应返回 200', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.get(`${API_URL}/system/dashboard/getCount`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBeTruthy();
  });

  test('10.5 已授权访问 dashboard 系统信息应返回 200', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.get(`${API_URL}/system/dashboard/getSystemInfo`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
    expect(body.data.cpuCount).toBeTruthy();
  });

  test('10.6 用户列表分页接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

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
    expect(Number(body.data.total)).toBeGreaterThan(0);
  });

  test('10.7 用户列表搜索接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.post(`${API_URL}/system/user/listByPage`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: { pageNo: 1, pageSize: 10, sysUser: { username: 'admin' } },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.records.length).toBeGreaterThan(0);
    expect(body.data.records[0].username).toBe('admin');
  });

  test('10.8 获取当前用户详情接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.get(`${API_URL}/system/user/getDetail`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
    expect(body.data.username).toBe('admin');
  });

  test('10.9 角色列表分页接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

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
    expect(body.data.records.length).toBeGreaterThan(0);
  });

  test('10.10 菜单树接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.post(`${API_URL}/system/menu/listByTree`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: { isMenu: 1 },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBeTruthy();
  });

  test('10.11 权限树接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.post(`${API_URL}/system/menu/listByTree`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: { isMenu: 0 },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBeTruthy();
  });

  test('10.12 字典树接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.post(`${API_URL}/system/dictionary/listByTree`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: {},
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    if (body.data) {
      expect(Array.isArray(body.data)).toBeTruthy();
    }
  });

  test('10.13 日志列表分页接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.post(`${API_URL}/system/log/listByPage`, {
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      data: { pageNo: 1, pageSize: 10, sysLog: {} },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.records).toBeTruthy();
    expect(Array.isArray(body.data.records)).toBeTruthy();
  });

  test('10.14 登录后访问 loginInfo 应返回用户信息', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.get(`${API_URL}/system/loginInfo`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
    expect(body.data.username).toBe('admin');
  });

  test('10.15 登录后访问用户菜单应返回菜单列表', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const response = await request.get(`${API_URL}/system/user/menus`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBeTruthy();
    expect(body.data.length).toBeGreaterThan(0);
  });

  test('10.16 登出接口应正常工作', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    const logoutRes = await request.post(`${API_URL}/system/logout`, {
      headers: { 'Authorization': token },
    });
    const body = await logoutRes.json();
    expect(body.code).toBe(200);
  });

  test('10.17 登出后 token 应失效', async ({ request }) => {
    const token = await apiLoginRequest(request, TEST_USER.username, TEST_USER.password);

    await request.post(`${API_URL}/system/logout`, {
      headers: { 'Authorization': token },
    });

    const response = await request.get(`${API_URL}/system/loginInfo`, {
      headers: { 'Authorization': token },
    });
    const body = await response.json();
    expect(body.code).toBe(4001);
  });
});
