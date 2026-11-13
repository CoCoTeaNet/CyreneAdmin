import {request, post} from '@/utils/axios-util';

/**
 * 新增菜单
 */
export function add(data: any) {
    return request('system/dictionary/add', data, post);
}

/**
 * 更新菜单
 * @param data
 * @returns {Promise<any>}
 */
export function update(data: any) {
    return request('system/dictionary/update', data, post);
}

/**
 * 批量删除菜单
 * @param data
 * @returns {Promise<*>}
 */
export function deleteBatch(data: any) {
    return request('system/dictionary/deleteBatch', data, post);
}

/**
 * 菜单列表
 */
export function listByPage(data: any) {
    return request('system/dictionary/listByPage', data, post);
}

/**
 * 菜单树
 */
export function listByTree(data: any) {
    return request('system/dictionary/listByTree', data, post);
}

/**
 * 启用字典缓存（按分类查找键分组）
 */
export function cache() {
    return request('system/dictionary/cache', {}, post);
}
