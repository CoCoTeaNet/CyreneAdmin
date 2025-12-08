package net.cocotea.cyreneadmin.service.impl;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.SysRoleAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysRolePageDTO;
import net.cocotea.cyreneadmin.model.dto.SysRoleUpdateDTO;
import net.cocotea.cyreneadmin.model.po.SysRole;
import net.cocotea.cyreneadmin.model.po.SysRoleMenu;
import net.cocotea.cyreneadmin.model.po.SysUserRole;
import net.cocotea.cyreneadmin.model.vo.SysRoleMenuVO;
import net.cocotea.cyreneadmin.model.vo.SysRoleVO;
import net.cocotea.cyreneadmin.service.SysRoleService;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 */
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final LightDao lightDao;

    @Override
    public boolean add(SysRoleAddDTO addDTO) throws BusinessException {
        SysRole sysRole = lightDao.convertType(addDTO, SysRole.class);
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("roleKey", addDTO.getRoleKey());
        SysRole existSysRole = lightDao.findOne("sys_role_findList", map, SysRole.class);
        if (existSysRole != null) {
            throw new BusinessException("已存在该角色标识");
        }
        Object id = lightDao.save(sysRole);
        return id != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        List<SysRole> sysRoleList = new ArrayList<>();
        for (BigInteger id : idList) {
            SysRole article = new SysRole();
            article.setId(id);
            article.setIsDeleted(IsEnum.Y.getCode());
            sysRoleList.add(article);
        }
        Long count = lightDao.updateAll(sysRoleList);
        return count > 0;
    }

    @Override
    public boolean update(SysRoleUpdateDTO updateDTO) {
        SysRole sysRole = lightDao.convertType(updateDTO, SysRole.class);
        Long update = lightDao.update(sysRole);
        return update > 0;
    }

    @Override
    public boolean grantPermissionsByRoleId(List<SysRoleMenuVO> sysRoleMenuVOList) throws BusinessException {
        List<SysRoleMenu> sysRoleMenuList = lightDao.convertType(sysRoleMenuVOList, SysRoleMenu.class);
        if (sysRoleMenuList.isEmpty()) {
            throw new BusinessException("集合为空");
        }
        // 先删除所有权限再设置
        BigInteger roleId = sysRoleMenuList.get(0).getRoleId();
        EntityQuery entityQuery = EntityQuery.create().where("#[role_id = :roleId]").names("roleId").values(roleId);
        lightDao.deleteByQuery(SysRoleMenu.class, entityQuery);
        // 重新添加权限
        long saved = lightDao.saveOrUpdateAll(sysRoleMenuList);
        return saved > 0;
    }

    @Override
    public List<SysRoleVO> loadByUserId(BigInteger userId) {
        // 角色与用户关联
        Map<String, Object> userRoleMap = MapUtil.newHashMap(1);
        userRoleMap.put("userId", userId);
        List<BigInteger> roleIds = lightDao.find("sys_user_role_findList", userRoleMap, SysUserRole.class).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 角色列表
        Map<String, Object> roleMap = MapUtil.newHashMap(1);
        roleMap.put("roleIds", roleIds);
        return lightDao.find("sys_role_findList", roleMap, SysRoleVO.class);
    }

    @Override
    public boolean delete(BigInteger id) {
        // 删除角色
        lightDao.delete(new SysRole().setId(id));
        // 删除角色权限关联关系
        EntityQuery entityQuery = EntityQuery.create().where("#[role_id = :roleId]").names("roleId").values(id);
        long deleted = lightDao.delete(entityQuery);
        return deleted > 0;
    }

    @Override
    public ApiPage<SysRoleVO> listByPage(SysRolePageDTO pageDTO) {
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("roleName", pageDTO.getSysRole().getRoleName());
        map.put("roleKey", pageDTO.getSysRole().getRoleKey());
        map.put("remark", pageDTO.getSysRole().getRemark());
        Page<SysRoleVO> page = lightDao.findPage(ApiPage.create(pageDTO), "sys_role_findList", map, SysRoleVO.class);
        return ApiPage.rest(page);
    }
}
