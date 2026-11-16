import {request, get, post} from '@/utils/axios-util';

/**
 * 新增菜单
 */
export function add(data: any) {
    return request('system/menu/add', data, post);
}

/**
 * 更新菜单
 * @param data
 * @returns {Promise<any>}
 */
export function update(data: any) {
    return request('system/menu/update', data, post);
}

/**
 * 批量删除菜单
 * @param data
 * @returns {Promise<*>}
 */
export function deleteBatch(data: any) {
    return request('system/menu/deleteBatch', data, post);
}

/**
 * 菜单列表
 */
export function listByPage(data: any) {
    return request('system/menu/listByPage', data, post);
}

/**
 * 菜单树
 */
export function listByTree(data: any) {
    return request('system/menu/listByTree', data, post);
}

export function listByTreeAsRoleSelection(data: any) {
    return request('system/menu/listByTreeAsRoleSelection', data, post);
}

/**
 * 通过角色获取已授权菜单（isMenu 分区：1菜单 0权限按钮）
 */
export function listByRoleId(roleId: string, isMenu: number) {
    return request(`system/menu/listByRoleId/${roleId}?isMenu=${isMenu}`, {}, get);
}
