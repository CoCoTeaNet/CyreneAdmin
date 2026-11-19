package net.cocotea.cyreneadmin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.SysMenuAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuTreeDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuUpdateDTO;
import net.cocotea.cyreneadmin.model.po.SysMenu;
import net.cocotea.cyreneadmin.model.po.SysRoleMenu;
import net.cocotea.cyreneadmin.model.po.SysUserRole;
import net.cocotea.cyreneadmin.model.vo.SysMenuTreeVO;
import net.cocotea.cyreneadmin.model.vo.SysMenuVO;
import net.cocotea.cyreneadmin.service.SysMenuService;
import net.cocotea.cyreneadmin.service.SysRoleService;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.lambda.LambdaQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.utils.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 * @since 2022-11-28 17:51:41
 */
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final LightDao lightDao;
    private final RedisService redisService;
    private final SysRoleService roleService;

    @Override
    public boolean add(SysMenuAddDTO param) {
        SysMenu sysMenu = lightDao.convertType(param, SysMenu.class);
        // 菜单
        if (StringUtil.isBlank(sysMenu.getParentId())) {
            sysMenu.setParentId(BigInteger.ZERO);
        }
        // 权限
        if (StringUtil.isBlank(sysMenu.getPermissionCode()) && StringUtil.isNotBlank(sysMenu.getRouterPath())) {
            sysMenu.setPermissionCode(sysMenu.getRouterPath().replace(CharPool.SLASH, CharPool.COLON));
        }
        Object menuId = lightDao.save(sysMenu);
        return menuId != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        idList.forEach(this::delete);
        // 菜单及权限编码已变化，清理所有用户的权限缓存
        clearAllPermissionCache();
        return true;
    }

    @Override
    public ApiPage<SysMenuVO> listByPage(SysMenuPageDTO pageDTO) {
        SysMenuPageDTO.Query query = pageDTO.getSysMenu();
        Page<SysMenu> page = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysMenu::getIsMenu, query.getIsMenu())
                .eq(true, SysMenu::getPermissionCode, query.getPermissionCode())
                .like(true, SysMenu::getMenuName, query.getMenuName())
                .eq(true, SysMenu::getMenuStatus, query.getMenuStatus())
                .orderBy(true, true, SysMenu::getSort)
                .orderBy(true, false, SysMenu::getId)
                .findPage(ApiPage.create(pageDTO));
        return ApiPage.rest(page, SysMenuVO.class);
    }

    @Override
    public List<Tree<BigInteger>> listByTree(SysMenuTreeDTO treeDTO) {
        return buildTree(findMenuVOList(treeDTO), treeDTO.getIsMenu());
    }

    private List<SysMenuVO> findMenuVOList(SysMenuTreeDTO treeDTO) {
        List<SysMenu> list = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysMenu::getIsMenu, treeDTO.getIsMenu())
                .eq(true, SysMenu::getPermissionCode, treeDTO.getPermissionCode())
                .like(true, SysMenu::getMenuName, treeDTO.getMenuName())
                .eq(true, SysMenu::getMenuStatus, treeDTO.getMenuStatus())
                .orderBy(true, true, SysMenu::getSort)
                .orderBy(true, false, SysMenu::getId)
                .find();
        return Convert.toList(SysMenuVO.class, list);
    }

    @Override
    public boolean update(SysMenuUpdateDTO param) {
        SysMenu sysMenu = lightDao.convertType(param, SysMenu.class);
        checkParentCycle(sysMenu);
        Long update = lightDao.update(sysMenu);
        // 权限编码可能变化，清理所有用户的权限缓存
        clearAllPermissionCache();
        return update > 0;
    }

    /**
     * 校验上级菜单不能是自身或自身的子孙节点，避免菜单树成环
     */
    private void checkParentCycle(SysMenu sysMenu) {
        BigInteger id = sysMenu.getId();
        BigInteger parentId = sysMenu.getParentId();
        if (id == null || parentId == null || BigInteger.ZERO.equals(parentId)) {
            return;
        }
        if (parentId.equals(id)) {
            throw new BusinessException("上级菜单不能选择自己");
        }
        Map<BigInteger, BigInteger> parentMap = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .find().stream()
                .collect(Collectors.toMap(
                        SysMenu::getId,
                        m -> m.getParentId() == null ? BigInteger.ZERO : m.getParentId(),
                        (a, b) -> a
                ));
        Set<BigInteger> visited = new HashSet<>();
        BigInteger cursor = parentId;
        while (cursor != null && !BigInteger.ZERO.equals(cursor)) {
            if (cursor.equals(id)) {
                throw new BusinessException("上级菜单不能选择自己的子菜单");
            }
            if (!visited.add(cursor)) {
                break;
            }
            cursor = parentMap.get(cursor);
        }
    }

    @Override
    public List<SysMenuVO> listByUserId(Integer isMenu) {
        // 1、获取登录用户ID
        BigInteger loginId = LoginUtils.loginId();
        // 2、查询用户的角色
        List<BigInteger> roleIds = LambdaQuery.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getUserId, loginId)
                .find().stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 空集合时 in 条件会被 sqltoy 忽略，必须提前返回避免查出全部菜单/权限
        if (roleIds.isEmpty()) {
            return List.of();
        }
        // 3、查询角色含有的菜单
        List<BigInteger> menuIds = LambdaQuery.create(SysRoleMenu.class).bind(lightDao)
                .in(true, SysRoleMenu::getRoleId, roleIds)
                .find().stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return List.of();
        }
        // 4、根据菜单ID查询菜单信息
        List<SysMenu> list = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .in(true, SysMenu::getId, menuIds)
                .eq(true, SysMenu::getIsMenu, isMenu)
                .orderBy(true, true, SysMenu::getSort)
                .orderBy(true, false, SysMenu::getId)
                .find();
        return Convert.toList(SysMenuVO.class, list);
    }

    @Override
    public boolean delete(BigInteger id) {
        SysMenu sysMenu = new SysMenu().setId(id).setIsDeleted(IsEnum.Y.getCode());
        Long update = lightDao.update(sysMenu);
        if (update <= 0) {
            return false;
        }
        // 获取子节点
        List<SysMenu> sysMenuList = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysMenu::getParentId, id)
                .orderBy(true, true, SysMenu::getSort)
                .orderBy(true, false, SysMenu::getId)
                .find();
        if (!sysMenuList.isEmpty()) {
            // 存在子节点，删除子节点
            sysMenuList.forEach(item -> delete(item.getId()));
        }
        return true;
    }

    @Override
    public List<SysMenuVO> listByRoleId(BigInteger roleId, Integer isMenu) {
        List<SysMenu> list = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(isMenu != null, SysMenu::getIsMenu, isMenu)
                .inSql(roleId != null, SysMenu::getId, "select menu_id from sys_role_menu where role_id = :roleId", MapUtil.of("roleId", roleId))
                .orderBy(true, true, SysMenu::getSort)
                .orderBy(true, false, SysMenu::getId)
                .find();
        return Convert.toList(SysMenuVO.class, list);
    }

    @Override
    public List<SysMenuVO> cachePermission(BigInteger userId) {
        // 缓存权限（过滤空权限码，避免鉴权集合出现 null）
        List<SysMenuVO> permissions = listByUserId(IsEnum.N.getCode()).stream()
                .filter(item -> StringUtil.isNotBlank(item.getPermissionCode()))
                .toList();
        redisService.save(String.format(RedisKeyConst.USER_PERMISSION, userId), JSONUtil.toJsonStr(permissions), 3600 * 24L);
        return permissions;
    }

    @Override
    public List<SysMenuVO> getCachePermission(BigInteger userId) {
        String s = redisService.get(String.format(RedisKeyConst.USER_PERMISSION, userId));
        // 缓存不存在时返回 null，由调用方回源并写入缓存
        if (StringUtil.isBlank(s)) {
            return null;
        }
        return JSONUtil.toList(s, SysMenuVO.class);
    }

    @Override
    public void clearAllPermissionCache() {
        redisService.deleteByPattern(String.format(RedisKeyConst.USER_PERMISSION, "*"));
    }

    @Override
    public List<Tree<BigInteger>> listByTreeAsRoleSelection(SysMenuTreeDTO treeDTO) {
        return buildTree(findMenuVOList(treeDTO), treeDTO.getIsMenu());
    }

    /**
     * 构建菜单树
     * <p>
     * 权限按钮(is_menu=0)的父节点是菜单节点，若只查询按钮本身，父节点缺失会导致
     * {@link TreeUtil#build} 整棵树为空，因此需要补齐祖先菜单作为分组节点。
     *
     * @param menuList 菜单/权限列表
     * @param isMenu   是否菜单，0=权限按钮
     * @return 树形结构
     */
    private List<Tree<BigInteger>> buildTree(List<SysMenuVO> menuList, Integer isMenu) {
        List<SysMenuVO> nodes = IsEnum.N.getCode().equals(isMenu) ? fillMenuAncestors(menuList) : menuList;
        List<TreeNode<BigInteger>> nodeList = nodes.stream()
                .map(menuVO -> {
                    TreeNode<BigInteger> node = new TreeNode<>(menuVO.getId(), menuVO.getParentId(), menuVO.getMenuName(), menuVO.getSort());
                    node.setExtra(BeanUtil.beanToMap(menuVO));
                    return node;
                }).toList();
        return TreeUtil.build(nodeList, BigInteger.ZERO);
    }

    /**
     * 补齐权限按钮缺失的祖先菜单节点，保证树结构可正常构建
     *
     * @param menuList 权限按钮列表
     * @return 含祖先菜单的节点列表
     */
    private List<SysMenuVO> fillMenuAncestors(List<SysMenuVO> menuList) {
        Map<BigInteger, SysMenuVO> nodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> nodeMap.put(menu.getId(), menu));
        Set<BigInteger> missingIds = collectMissingParentIds(nodeMap);
        // 逐层向上补齐，直到根节点或父节点已被删除
        while (!missingIds.isEmpty()) {
            List<SysMenu> parents = LambdaQuery.create(SysMenu.class).bind(lightDao)
                    .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                    .in(true, SysMenu::getId, missingIds)
                    .find();
            if (parents.isEmpty()) {
                break;
            }
            Convert.toList(SysMenuVO.class, parents).forEach(parent -> nodeMap.putIfAbsent(parent.getId(), parent));
            missingIds = collectMissingParentIds(nodeMap);
        }
        return new ArrayList<>(nodeMap.values());
    }

    private Set<BigInteger> collectMissingParentIds(Map<BigInteger, SysMenuVO> nodeMap) {
        return nodeMap.values().stream()
                .map(SysMenuVO::getParentId)
                .filter(parentId -> parentId != null && !BigInteger.ZERO.equals(parentId))
                .filter(parentId -> !nodeMap.containsKey(parentId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<Tree<BigInteger>> userMenu() {
        // 超级管理员不受授权关联约束，直接返回全部可见菜单
        if (roleService.isSuperAdmin(LoginUtils.loginId())) {
            SysMenuTreeDTO treeDTO = new SysMenuTreeDTO();
            treeDTO.setIsMenu(IsEnum.Y.getCode());
            treeDTO.setMenuStatus(0);
            return listByTree(treeDTO);
        }
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("userIds", List.of(LoginUtils.loginId()))
                .build();
        List<TreeNode<BigInteger>> nodeList = lightDao.find("sys_menu_findUserMenu", params, SysMenuTreeVO.class)
                .stream()
                .map(menu -> {
                    TreeNode<BigInteger> node = new TreeNode<>(menu.getId(), menu.getParentId(), menu.getMenuName(), menu.getSort());
                    node.setExtra(BeanUtil.beanToMap(menu));
                    return node;
                })
                .toList();
        return TreeUtil.build(nodeList, BigInteger.ZERO);
    }

}
