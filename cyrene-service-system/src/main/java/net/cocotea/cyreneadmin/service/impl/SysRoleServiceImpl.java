package net.cocotea.cyreneadmin.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.SysRoleAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysRoleGrantDTO;
import net.cocotea.cyreneadmin.model.dto.SysRolePageDTO;
import net.cocotea.cyreneadmin.model.dto.SysRoleUpdateDTO;
import net.cocotea.cyreneadmin.model.po.SysMenu;
import net.cocotea.cyreneadmin.model.po.SysRole;
import net.cocotea.cyreneadmin.model.po.SysRoleMenu;
import net.cocotea.cyreneadmin.model.po.SysUserRole;
import net.cocotea.cyreneadmin.model.vo.SysRoleVO;
import net.cocotea.cyreneadmin.service.SysRoleService;
import net.cocotea.cyreneadmin.constant.GlobalConst;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.lambda.LambdaDelete;
import org.sagacity.sqltoy.lambda.LambdaQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.utils.StringUtil;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 */
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final LightDao lightDao;
    private final RedisService redisService;

    @Override
    public boolean add(SysRoleAddDTO addDTO) throws BusinessException {
        // 超管角色标识是系统唯一提权入口，只允许超管创建
        if (GlobalConst.ROLE_SUPER_ADMIN.equals(addDTO.getRoleKey()) && !isOperatorSuperAdmin()) {
            throw new BusinessException("无权创建超级管理员角色");
        }
        SysRole sysRole = lightDao.convertType(addDTO, SysRole.class);
        SysRole existSysRole = LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysRole::getRoleKey, addDTO.getRoleKey())
                .getOne();
        if (existSysRole != null) {
            throw new BusinessException("已存在该角色标识");
        }
        Object id = lightDao.save(sysRole);
        return id != null;
    }

    @Tran
    @Transactional
    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        idList.forEach(this::delete);
        return true;
    }

    @Override
    public boolean update(SysRoleUpdateDTO updateDTO) {
        BigInteger roleId = new BigInteger(updateDTO.getId());
        SysRole current = loadRole(roleId);
        if (current == null) {
            throw new BusinessException("角色不存在");
        }
        // 超级管理员角色标识不允许变更，避免系统失去唯一超管入口
        boolean changeRoleKey = StringUtil.isNotBlank(updateDTO.getRoleKey());
        // 非超管不能修改超管角色，也不能把已有角色改成超管标识
        if (!isOperatorSuperAdmin()
                && (GlobalConst.ROLE_SUPER_ADMIN.equals(current.getRoleKey())
                || GlobalConst.ROLE_SUPER_ADMIN.equals(updateDTO.getRoleKey()))) {
            throw new BusinessException("无权修改超级管理员角色");
        }
        if (GlobalConst.ROLE_SUPER_ADMIN.equals(current.getRoleKey())
                && changeRoleKey
                && !GlobalConst.ROLE_SUPER_ADMIN.equals(updateDTO.getRoleKey())) {
            throw new BusinessException("超级管理员角色标识不允许修改");
        }
        if (changeRoleKey) {
            SysRole existSysRole = LambdaQuery.create(SysRole.class).bind(lightDao)
                    .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                    .eq(true, SysRole::getRoleKey, updateDTO.getRoleKey())
                    .ne(true, SysRole::getId, roleId)
                    .getOne();
            if (existSysRole != null) {
                throw new BusinessException("已存在该角色标识");
            }
        }
        SysRole sysRole = lightDao.convertType(updateDTO, SysRole.class);
        Long update = lightDao.update(sysRole);
        // 角色标识变化会影响角色鉴权，清理该角色下用户的权限缓存
        clearUserPermissionCache(roleId);
        return update > 0;
    }

    @Tran
    @Transactional
    @Override
    public boolean grantPermissionsByRoleId(SysRoleGrantDTO grantDTO) throws BusinessException {
        BigInteger roleId = new BigInteger(grantDTO.getRoleId());
        SysRole targetRole = loadRole(roleId);
        if (targetRole == null) {
            throw new BusinessException("角色不存在");
        }
        // 超管角色默认拥有全部权限，授权数据对其无意义且会被误用为提权入口
        if (GlobalConst.ROLE_SUPER_ADMIN.equals(targetRole.getRoleKey())) {
            throw new BusinessException("超级管理员角色默认拥有全部权限，无需授权");
        }
        // 校验授权项与分区一致，避免通过“权限分区”写入菜单导致授权数据越界
        List<String> menuIds = grantDTO.getMenuIds();
        List<BigInteger> grantMenuIds = menuIds == null ? List.of() : menuIds.stream()
                .map(BigInteger::new)
                .distinct()
                .toList();
        if (!grantMenuIds.isEmpty()) {
            long validCount = LambdaQuery.create(SysMenu.class).bind(lightDao)
                    .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                    .eq(true, SysMenu::getIsMenu, grantDTO.getIsMenu())
                    .in(true, SysMenu::getId, grantMenuIds)
                    .count();
            if (validCount != grantMenuIds.size()) {
                throw new BusinessException("存在不属于当前授权分区的菜单");
            }
            // 非超管只能授予自身已拥有的菜单或按钮，避免给自己或他人提权
            if (!isOperatorSuperAdmin() && !ownGrantedIds(grantDTO.getIsMenu()).containsAll(grantMenuIds)) {
                throw new BusinessException("无权授予超出自身权限的菜单或按钮");
            }
        }
        // 只替换当前分区（菜单或权限按钮），避免两类授权互相覆盖
        LambdaDelete.create(SysRoleMenu.class).bind(lightDao)
                .eq(true, SysRoleMenu::getRoleId, roleId)
                .inSql(true, SysRoleMenu::getMenuId, "select id from sys_menu where is_menu = :isMenu",
                        MapUtil.of("isMenu", grantDTO.getIsMenu()))
                .delete();
        boolean result = true;
        if (!grantMenuIds.isEmpty()) {
            List<SysRoleMenu> sysRoleMenuList = grantMenuIds.stream()
                    .map(menuId -> new SysRoleMenu().setRoleId(roleId).setMenuId(menuId))
                    .collect(Collectors.toList());
            result = lightDao.saveOrUpdateAll(sysRoleMenuList) > 0;
        }
        // 授权变化后清理该角色下用户的权限缓存，避免继续使用旧权限
        clearUserPermissionCache(roleId);
        return result;
    }

    @Override
    public List<SysRoleVO> loadByUserId(BigInteger userId) {
        // 角色与用户关联
        List<BigInteger> roleIds = LambdaQuery.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getUserId, userId)
                .find().stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 空集合时 in 条件会被 sqltoy 忽略，必须提前返回避免查出全部角色
        if (roleIds.isEmpty()) {
            return List.of();
        }
        // 角色列表
        List<SysRole> roleList = LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .in(true, SysRole::getId, roleIds)
                .find();
        return Convert.toList(SysRoleVO.class, roleList);
    }

    @Override
    public boolean isSuperAdmin(BigInteger userId) {
        return loadByUserId(userId).stream()
                .anyMatch(role -> GlobalConst.ROLE_SUPER_ADMIN.equals(role.getRoleKey()));
    }

    @Tran
    @Transactional
    @Override
    public boolean delete(BigInteger id) {
        SysRole current = loadRole(id);
        if (current == null) {
            return false;
        }
        if (GlobalConst.ROLE_SUPER_ADMIN.equals(current.getRoleKey())) {
            throw new BusinessException("超级管理员角色不允许删除");
        }
        // 假删除角色
        SysRole sysRole = new SysRole().setId(id).setIsDeleted(IsEnum.Y.getCode());
        Long updated = lightDao.update(sysRole);
        // 清理角色权限与用户角色关联
        LambdaDelete.create(SysRoleMenu.class).bind(lightDao)
                .eq(true, SysRoleMenu::getRoleId, id)
                .delete();
        // 必须在删除用户角色关联前清理缓存，否则已查不到该角色下的用户
        clearUserPermissionCache(id);
        LambdaDelete.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getRoleId, id)
                .delete();
        return updated > 0;
    }

    private SysRole loadRole(BigInteger id) {
        return LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysRole::getId, id)
                .getOne();
    }

    private boolean isOperatorSuperAdmin() {
        BigInteger operatorId = LoginUtils.loginId();
        return operatorId != null && isSuperAdmin(operatorId);
    }

    /**
     * 操作者自身在当前授权分区内已拥有的菜单或按钮ID
     */
    private Set<BigInteger> ownGrantedIds(Integer isMenu) {
        BigInteger operatorId = LoginUtils.loginId();
        if (operatorId == null) {
            return Set.of();
        }
        List<BigInteger> roleIds = LambdaQuery.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getUserId, operatorId)
                .find().stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return LambdaQuery.create(SysRoleMenu.class).bind(lightDao)
                .in(true, SysRoleMenu::getRoleId, roleIds)
                .inSql(true, SysRoleMenu::getMenuId, "select id from sys_menu where is_menu = :isMenu",
                        MapUtil.of("isMenu", isMenu))
                .find().stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
    }

    /**
     * 清理某角色下所有用户的权限缓存
     */
    private void clearUserPermissionCache(BigInteger roleId) {
        List<BigInteger> userIds = LambdaQuery.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getRoleId, roleId)
                .find().stream().map(SysUserRole::getUserId).toList();
        userIds.forEach(userId ->
                redisService.delete(String.format(RedisKeyConst.USER_PERMISSION, userId)));
    }

    @Override
    public ApiPage<SysRoleVO> listByPage(SysRolePageDTO pageDTO) {
        SysRolePageDTO.Query query = pageDTO.getSysRole();
        Page<SysRole> page = LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysRole::getRoleKey, query.getRoleKey())
                .like(true, SysRole::getRoleName, query.getRoleName())
                .like(true, SysRole::getRemark, query.getRemark())
                .orderBy(true, false, SysRole::getId)
                .findPage(ApiPage.create(pageDTO));
        return ApiPage.rest(page, SysRoleVO.class);
    }
}
