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
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 * @since 2022-11-28 17:51:41
 */
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {
    private static final Logger logger = LoggerFactory.getLogger(SysMenuServiceImpl.class);

    private final LightDao lightDao;
    private final RedisService redisService;

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
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public ApiPage<SysMenuVO> listByPage(SysMenuPageDTO pageDTO) {
        Map<String, Object> map = BeanUtil.beanToMap(pageDTO.getSysMenu());
        Page<Object> fastPage = ApiPage.create(pageDTO);
        Page<SysMenu> page = lightDao.findPage(fastPage, "sys_menu_findList", map, SysMenu.class);
        return ApiPage.rest(page, SysMenuVO.class);
    }

    @Override
    public List<Tree<BigInteger>> listByTree(SysMenuTreeDTO treeDTO) {
        Map<String, Object> map = BeanUtil.beanToMap(treeDTO);
        List<SysMenuVO> list = lightDao.find("sys_menu_findList", map, SysMenuVO.class);

        List<TreeNode<BigInteger>> nodeList = list.stream()
                .map(menuVO -> {
                    TreeNode<BigInteger> node = new TreeNode<>(menuVO.getId(), menuVO.getParentId(), menuVO.getMenuName(), menuVO.getSort());
                    node.setExtra(BeanUtil.beanToMap(menuVO));
                    return node;
                }).toList();

        return TreeUtil.build(nodeList, BigInteger.ZERO);
    }

    @Override
    public boolean update(SysMenuUpdateDTO param) {
        SysMenu sysMenu = lightDao.convertType(param, SysMenu.class);
        Long update = lightDao.update(sysMenu);
        return update > 0;
    }

    @Override
    public List<SysMenuVO> listByUserId(Integer isMenu) {
        // 1、获取登录用户ID
        BigInteger loginId = LoginUtils.loginId();
        // 2、查询用户的角色
        List<BigInteger> roleIds = lightDao.find("sys_user_role_findList", new SysUserRole().setUserId(loginId), SysUserRole.class).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 3、查询角色含有的菜单
        Map<String, Object> sysRoleMenuMap = new HashMap<>(1);
        sysRoleMenuMap.put("roleIds", roleIds);
        List<BigInteger> menuIds = lightDao.find("sys_role_menu_findList", sysRoleMenuMap, SysRoleMenu.class).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        // 4、根据菜单ID查询菜单信息
        Map<String, Object> sysMenuMap = new HashMap<>(2);
        sysMenuMap.put("menuIds", menuIds);
        sysMenuMap.put("isMenu", isMenu);
        List<SysMenu> list = lightDao.find("sys_menu_findList", sysMenuMap, SysMenu.class);
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
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("parentId", id);
        List<SysMenu> sysMenuList = lightDao.find("sys_menu_findList", map, SysMenu.class);
        if (!sysMenuList.isEmpty()) {
            // 存在子节点，删除子节点
            sysMenuList.forEach(item -> delete(item.getId()));
        }
        return true;
    }

    @Override
    public List<SysMenuVO> listByRoleId(BigInteger roleId) {
        Map<String, Object> sysMenuMap = MapUtil.newHashMap(1);
        sysMenuMap.put("roleId", roleId);
        List<SysMenu> list = lightDao.find("sys_menu_IN_findList", sysMenuMap, SysMenu.class);
        return Convert.toList(SysMenuVO.class, list);
    }

    @Override
    public List<SysMenuVO> cachePermission(BigInteger userId) {
        // 缓存权限
        List<SysMenuVO> permissions = listByUserId(IsEnum.N.getCode());
        redisService.save(String.format(RedisKeyConst.USER_PERMISSION, userId), JSONUtil.toJsonStr(permissions), 3600 * 24L);
        return permissions;
    }

    @Override
    public List<SysMenuVO> getCachePermission(BigInteger userId) {
        String s = redisService.get(String.format(RedisKeyConst.USER_PERMISSION, userId));
        logger.info("[{}]-permissions={}", userId, s);
        return JSONUtil.toList(s, SysMenuVO.class);
    }

    @Override
    public List<Tree<BigInteger>> listByTreeAsRoleSelection(SysMenuTreeDTO treeDTO) {
        Map<String, Object> map = BeanUtil.beanToMap(treeDTO);
        List<TreeNode<BigInteger>> nodeList = lightDao.find("sys_menu_findList", map, SysMenuVO.class)
                .stream()
                .map(menuVO -> {
                    TreeNode<BigInteger> node = new TreeNode<>(menuVO.getId(), menuVO.getParentId(), menuVO.getMenuName(), menuVO.getSort());
                    node.setExtra(BeanUtil.beanToMap(menuVO));
                    return node;
                }).toList();
        return TreeUtil.build(nodeList, BigInteger.ZERO);
    }

    @Override
    public List<Tree<BigInteger>> userMenu() {
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
