package net.cocotea.cyreneadmin.service;

import net.cocotea.cyreneadmin.model.dto.SysRoleAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysRoleGrantDTO;
import net.cocotea.cyreneadmin.model.dto.SysRolePageDTO;
import net.cocotea.cyreneadmin.model.dto.SysRoleUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysRoleVO;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;

import java.math.BigInteger;
import java.util.List;

/**
 * 角色服务类
 * @date 2022-1-17 17:14:06
 * @author CoCoTea
 */
public interface SysRoleService extends BaseService<ApiPage<SysRoleVO>, SysRolePageDTO, SysRoleAddDTO, SysRoleUpdateDTO> {
    /**
     * 给角色赋予权限（按菜单/权限分区替换，空集合清空该分区）
     * @param grantDTO 授权入参
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    boolean grantPermissionsByRoleId(SysRoleGrantDTO grantDTO) throws BusinessException;

    /**
     * 通过用户ID获取角色
     * @param userId 用户id
     * @return 角色
     */
    List<SysRoleVO> loadByUserId(BigInteger userId);

    /**
     * 是否超级管理员（拥有全部菜单与权限，不受授权关联约束）
     * @param userId 用户id
     * @return 是返回true
     */
    boolean isSuperAdmin(BigInteger userId);
}
