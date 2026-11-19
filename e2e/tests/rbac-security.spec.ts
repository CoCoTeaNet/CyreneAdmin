import { test, expect } from '@playwright/test';
import { apiLoginRequest } from '../utils/auth';
import { API_URL, TEST_PASSWORD } from '../utils/config';

const REGULAR_ROLE_ID = '1150175648716701696';
const suffix = Date.now();

function auth(token: string) {
  return { 'Authorization': token, 'Content-Type': 'application/json' };
}

async function post(request: any, token: string, path: string, data?: any) {
  const res = await request.post(`${API_URL}${path}`, { headers: auth(token), data });
  return res.json();
}

async function get(request: any, token: string, path: string) {
  const res = await request.get(`${API_URL}${path}`, { headers: auth(token) });
  return res.json();
}

function flattenNodes(nodes: any[]): any[] {
  const list: any[] = [];
  const walk = (arr: any[]) => {
    for (const n of arr ?? []) {
      list.push(n);
      walk(n.children);
    }
  };
  walk(nodes);
  return list;
}

function flattenIds(nodes: any[]): string[] {
  return flattenNodes(nodes).map((n) => String(n.id));
}

async function findUserId(request: any, token: string, username: string) {
  const body = await post(request, token, '/system/user/listByPage', {
    pageNo: 1, pageSize: 10, sysUser: { username },
  });
  return body.data.records.find((r: any) => r.username === username)?.id;
}

test.describe('P0 安全与授权回归', () => {
  test('空密码新增用户应以默认密码哈希落库并可登录', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const username = `p0pwd${suffix}`;
    const add = await post(request, token, '/system/user/add', {
      username, nickname: 'P0默认密码', sex: '1', roleIds: [REGULAR_ROLE_ID], accountStatus: '1',
    });
    expect(add.code).toBe(200);

    const loginToken = await apiLoginRequest(request, username, TEST_PASSWORD);
    expect(loginToken).toBeTruthy();

    const userId = await findUserId(request, token, username);
    await post(request, token, `/system/user/delete/${userId}`);
  });

  test('重复用户名新增应被拒绝', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const add = await post(request, token, '/system/user/add', {
      username: 'admin', nickname: 'dup', sex: '1', roleIds: [REGULAR_ROLE_ID],
    });
    expect(add.code).not.toBe(200);
    expect(add.message).toContain('已存在该用户名');
  });

  test('停用账号禁止登录，恢复后放行，冻结后已登录会话失效', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const username = `p0stat${suffix}`;
    const add = await post(request, token, '/system/user/add', {
      username, nickname: 'P0状态', sex: '1', roleIds: [REGULAR_ROLE_ID], accountStatus: '0',
    });
    expect(add.code).toBe(200);
    const userId = await findUserId(request, token, username);

    const { getVerifyCode } = await import('../utils/redis');
    const { sm2Encrypt } = await import('../utils/sm2');
    const loginWith = async (pwd: string) => {
      // 验证码为一次性使用，每次登录尝试都需重新获取
      const captchaRes = await request.get(`${API_URL}/system/captcha?timestamp=${Date.now()}`);
      const { captchaId, publicKey } = (await captchaRes.json()).data;
      const res = await request.post(`${API_URL}/system/login`, {
        data: {
          username, password: sm2Encrypt(publicKey, pwd),
          captcha: await getVerifyCode(captchaId), captchaId, publicKey, rememberMe: false,
        },
      });
      return res.json();
    };

    const disabled = await loginWith(TEST_PASSWORD);
    expect(disabled.code).not.toBe(200);
    expect(disabled.message).toContain('账号状态异常');

    const enable = await post(request, token, '/system/user/update', {
      id: userId, username, roleIds: [REGULAR_ROLE_ID], accountStatus: '1',
    });
    expect(enable.code).toBe(200);
    const ok = await loginWith(TEST_PASSWORD);
    expect(ok.code).toBe(200);

    const freeze = await post(request, token, '/system/user/update', {
      id: userId, username, roleIds: [REGULAR_ROLE_ID], accountStatus: '2',
    });
    expect(freeze.code).toBe(200);
    const session = await get(request, ok.data, '/system/loginInfo');
    expect(session.code).not.toBe(200);
    const after = await get(request, ok.data, '/system/loginInfo');
    expect(after.code).toBe(4001);

    await post(request, token, `/system/user/delete/${userId}`);
  });

  test('角色授权按分区替换、空集合清空、删除角色清理用户关联', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const roleName = `P0Temp${suffix}`;
    const addRole = await post(request, token, '/system/role/add', {
      roleName, roleKey: `role:p0:${suffix}`, sort: 99,
    });
    expect(addRole.code).toBe(200);
    const rolePage = await post(request, token, '/system/role/listByPage', {
      pageNo: 1, pageSize: 10, sysRole: { roleName },
    });
    const roleId = rolePage.data.records.find((r: any) => r.roleName === roleName)?.id;
    expect(roleId).toBeTruthy();

    const menuTree = await post(request, token, '/system/menu/listByTreeAsRoleSelection', { isMenu: 1 });
    const permTree = await post(request, token, '/system/menu/listByTreeAsRoleSelection', { isMenu: 0 });
    const menuIds = flattenIds(menuTree.data);
    // 权限树包含用于分组展示的祖先菜单节点，授权只取 isMenu=0 的按钮
    const permIds = flattenNodes(permTree.data)
      .filter((n: any) => String(n.isMenu) === '0')
      .map((n: any) => String(n.id));
    expect(menuIds.length).toBeGreaterThan(1);
    expect(permIds.length).toBeGreaterThan(0);
    const grantMenuIds = menuIds.slice(0, 2);
    const grantPermIds = permIds.slice(0, 1);

    const granted = async (isMenu: number) => {
      const body = await get(request, token, `/system/menu/listByRoleId/${roleId}?isMenu=${isMenu}`);
      return body.data.map((m: any) => String(m.id)).sort();
    };

    const g1 = await post(request, token, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 1, menuIds: grantMenuIds,
    });
    expect(g1.code).toBe(200);
    expect(await granted(1)).toEqual([...grantMenuIds].sort());
    expect(await granted(0)).toEqual([]);

    const g2 = await post(request, token, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 0, menuIds: grantPermIds,
    });
    expect(g2.code).toBe(200);
    expect(await granted(1)).toEqual([...grantMenuIds].sort());
    expect(await granted(0)).toEqual([...grantPermIds].sort());

    const g3 = await post(request, token, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 0, menuIds: [],
    });
    expect(g3.code).toBe(200);
    expect(await granted(0)).toEqual([]);
    expect(await granted(1)).toEqual([...grantMenuIds].sort());

    const username = `p0grant${suffix}`;
    const addUser = await post(request, token, '/system/user/add', {
      username, nickname: 'P0授权', sex: '1', roleIds: [roleId], accountStatus: '1',
    });
    expect(addUser.code).toBe(200);
    const userToken = await apiLoginRequest(request, username, TEST_PASSWORD);
    const menusBefore = await get(request, userToken, '/system/user/menus');
    expect(menusBefore.code).toBe(200);
    expect(flattenIds(menusBefore.data).length).toBeGreaterThan(0);

    const delRole = await post(request, token, `/system/role/delete/${roleId}`);
    expect(delRole.code).toBe(200);
    const menusAfter = await get(request, userToken, '/system/user/menus');
    expect(menusAfter.code).toBe(200);
    expect(flattenIds(menusAfter.data)).toEqual([]);

    const userId = await findUserId(request, token, username);
    await post(request, token, `/system/user/delete/${userId}`);
  });

  test('写接口按权限码鉴权：未授予拒绝，授予后放行', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const roleName = `P1Perm${suffix}`;
    const addRole = await post(request, token, '/system/role/add', {
      roleName, roleKey: `role:p1:${suffix}`, sort: 99,
    });
    expect(addRole.code).toBe(200);
    const rolePage = await post(request, token, '/system/role/listByPage', {
      pageNo: 1, pageSize: 10, sysRole: { roleName },
    });
    const roleId = rolePage.data.records.find((r: any) => r.roleName === roleName)?.id;
    expect(roleId).toBeTruthy();

    const username = `p1perm${suffix}`;
    const addUser = await post(request, token, '/system/user/add', {
      username, nickname: 'P1权限码', sex: '1', roleIds: [roleId], accountStatus: '1',
    });
    expect(addUser.code).toBe(200);
    const userToken = await apiLoginRequest(request, username, TEST_PASSWORD);

    const denied = await post(request, userToken, '/system/role/delete/999999999999999999');
    expect(denied.code).toBe(4002);

    const grant = await post(request, token, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 0, menuIds: ['9100000000000000203'],
    });
    expect(grant.code).toBe(200);
    const allowed = await post(request, userToken, '/system/role/delete/999999999999999999');
    expect(allowed.code).toBe(200);

    const userId = await findUserId(request, token, username);
    await post(request, token, `/system/user/delete/${userId}`);
    await post(request, token, `/system/role/delete/${roleId}`);
  });

  test('非超管禁止提权：不得授予超管角色、篡改超管标识或越权授权', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const SUPER_ROLE_ID = '1699771910151151616';

    const roleName = `P0Ops${suffix}`;
    const roleKey = `role:p0ops:${suffix}`;
    expect((await post(request, token, '/system/role/add', { roleName, roleKey, sort: 99 })).code).toBe(200);
    const rolePage = await post(request, token, '/system/role/listByPage', {
      pageNo: 1, pageSize: 10, sysRole: { roleName },
    });
    const roleId = rolePage.data.records.find((r: any) => r.roleName === roleName)?.id;
    expect(roleId).toBeTruthy();

    // 运维角色：用户新增/更新、角色新增/更新/授权
    const opsButtons = [
      '9100000000000000101', '9100000000000000102',
      '9100000000000000201', '9100000000000000202', '9100000000000000204',
    ];
    const grantOps = await post(request, token, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 0, menuIds: opsButtons,
    });
    expect(grantOps.code).toBe(200);

    const username = `p0ops${suffix}`;
    const addOps = await post(request, token, '/system/user/add', {
      username, nickname: 'P0提权', sex: '1', roleIds: [roleId], accountStatus: '1',
    });
    expect(addOps.code).toBe(200);
    const userId = await findUserId(request, token, username);
    const opsToken = await apiLoginRequest(request, username, TEST_PASSWORD);

    // 1. 把自己升级成超管
    const selfEscalate = await post(request, opsToken, '/system/user/update', {
      id: userId, username, roleIds: [roleId, SUPER_ROLE_ID],
    });
    expect(selfEscalate.code).not.toBe(200);
    expect(selfEscalate.message).toContain('不能修改自己的角色');

    // 2. 把普通用户升级成超管
    const victim = `p0victim${suffix}`;
    const addVictim = await post(request, token, '/system/user/add', {
      username: victim, nickname: 'P0目标', sex: '1', roleIds: [roleId], accountStatus: '1',
    });
    expect(addVictim.code).toBe(200);
    const victimId = await findUserId(request, token, victim);
    const victimEscalate = await post(request, opsToken, '/system/user/update', {
      id: victimId, username: victim, roleIds: [SUPER_ROLE_ID],
    });
    expect(victimEscalate.code).not.toBe(200);
    expect(victimEscalate.message).toContain('超出自身权限');

    // 自身角色范围内维护他人仍然放行
    const victimOk = await post(request, opsToken, '/system/user/update', {
      id: victimId, username: victim, nickname: 'P0目标改', roleIds: [roleId],
    });
    expect(victimOk.code).toBe(200);

    // 3. 把自己的角色标识改成超管
    const hijackKey = await post(request, opsToken, '/system/role/update', {
      id: roleId, roleName, roleKey: 'role:super:admin',
    });
    expect(hijackKey.code).not.toBe(200);
    expect(hijackKey.message).toContain('超级管理员');

    // 4. 直接创建超管角色
    const forgeRole = await post(request, opsToken, '/system/role/add', {
      roleName: `P0Forge${suffix}`, roleKey: 'role:super:admin', sort: 99,
    });
    expect(forgeRole.code).not.toBe(200);
    expect(forgeRole.message).toContain('超级管理员');

    // 5. 授予自己没有的按钮
    const overGrant = await post(request, opsToken, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 0, menuIds: ['9100000000000000303'],
    });
    expect(overGrant.code).not.toBe(200);
    expect(overGrant.message).toContain('超出自身权限');

    // 6. 授予自己已有的按钮放行
    const ownGrant = await post(request, opsToken, '/system/role/grantPermissionsByRoleId', {
      roleId, isMenu: 0, menuIds: opsButtons,
    });
    expect(ownGrant.code).toBe(200);

    // 7. 非超管不能改密或删除超管账号
    const adminPage = await post(request, token, '/system/user/listByPage', {
      pageNo: 1, pageSize: 10, sysUser: { username: 'admin' },
    });
    const adminId = adminPage.data.records.find((r: any) => r.username === 'admin')?.id;
    const touchAdmin = await post(request, opsToken, '/system/user/update', {
      id: adminId, username: 'admin', nickname: 'hacked', roleIds: [SUPER_ROLE_ID],
    });
    expect(touchAdmin.code).not.toBe(200);
    expect(touchAdmin.message).toContain('无权操作该用户');

    await post(request, token, `/system/user/delete/${victimId}`);
    await post(request, token, `/system/user/delete/${userId}`);
    await post(request, token, `/system/role/delete/${roleId}`);
  });

  test('头像上传接口需登录', async ({ request }) => {
    const res = await request.post(`${API_URL}/system/user/avatar/upload`, {
      multipart: {
        file: {
          name: 'avatar.png',
          mimeType: 'image/png',
          buffer: Buffer.from(
            'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==',
            'base64',
          ),
        },
      },
    });
    expect((await res.json()).code).toBe(4001);
  });

  test('更新为已存在用户名应被拒绝，保持自身用户名可更新', async ({ request }) => {
    const token = await apiLoginRequest(request);
    const username = `p1dup${suffix}`;
    const add = await post(request, token, '/system/user/add', {
      username, nickname: 'P1重名', sex: '1', roleIds: [REGULAR_ROLE_ID], accountStatus: '1',
    });
    expect(add.code).toBe(200);
    const userId = await findUserId(request, token, username);

    const dup = await post(request, token, '/system/user/update', {
      id: userId, username: 'admin', roleIds: [REGULAR_ROLE_ID],
    });
    expect(dup.code).not.toBe(200);
    expect(dup.message).toContain('已存在该用户名');

    const self = await post(request, token, '/system/user/update', {
      id: userId, username, nickname: 'P1重名改', roleIds: [REGULAR_ROLE_ID],
    });
    expect(self.code).toBe(200);

    await post(request, token, `/system/user/delete/${userId}`);
  });
});