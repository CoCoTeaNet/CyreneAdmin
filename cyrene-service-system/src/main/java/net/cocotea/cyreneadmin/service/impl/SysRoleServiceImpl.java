package net.cocotea.cyreneadmin.service.impl;

import cn.hutool.core.convert.Convert;
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
import org.sagacity.sqltoy.lambda.LambdaDelete;
import org.sagacity.sqltoy.lambda.LambdaQuery;
import org.sagacity.sqltoy.model.Page;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
        LambdaDelete.create(SysRoleMenu.class).bind(lightDao)
                .eq(true, SysRoleMenu::getRoleId, roleId)
                .delete();
        // 重新添加权限
        long saved = lightDao.saveOrUpdateAll(sysRoleMenuList);
        return saved > 0;
    }

    @Override
    public List<SysRoleVO> loadByUserId(BigInteger userId) {
        // 角色与用户关联
        List<BigInteger> roleIds = LambdaQuery.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getUserId, userId)
                .find().stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 角色列表
        List<SysRole> roleList = LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .in(true, SysRole::getId, roleIds)
                .find();
        return Convert.toList(SysRoleVO.class, roleList);
    }

    @Override
    public boolean delete(BigInteger id) {
        // 删除角色
        lightDao.delete(new SysRole().setId(id));
        // 删除角色权限关联关系
        long deleted = LambdaDelete.create(SysRoleMenu.class).bind(lightDao)
                .eq(true, SysRoleMenu::getRoleId, id)
                .delete();
        return deleted > 0;
    }

    @Override
    public ApiPage<SysRoleVO> listByPage(SysRolePageDTO pageDTO) {
        SysRolePageDTO.Query query = pageDTO.getSysRole();
        Page<SysRole> page = LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysRole::getRoleKey, query.getRoleKey())
                .like(true, SysRole::getRoleName, query.getRoleName())
                .like(true, SysRole::getRemark, query.getRemark())
                .findPage(ApiPage.create(pageDTO));
        return ApiPage.rest(page, SysRoleVO.class);
    }
}
