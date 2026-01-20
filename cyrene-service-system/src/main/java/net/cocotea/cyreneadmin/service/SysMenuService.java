package net.cocotea.cyreneadmin.service;

import cn.hutool.core.lang.tree.Tree;
import net.cocotea.cyreneadmin.model.dto.SysMenuAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuTreeDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysMenuVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

/**
 * @author CoCoTea
 * @date 2022-1-16 15:47:03
 */
public interface SysMenuService extends BaseService<ApiPage<SysMenuVO>, SysMenuPageDTO, SysMenuAddDTO, SysMenuUpdateDTO> {
    /**
     * 分页查询菜单
     *
     * @param menuTreeDTO 分页参数
     * @return 分页结果集
     */
    List<Tree<BigInteger>> listByTree(SysMenuTreeDTO menuTreeDTO);

    /**
     * 获取用户的所有菜单
     *
     * @param isMenu 是否菜单
     * @return 用户菜单集合
     */
    List<SysMenuVO> listByUserId(Integer isMenu);

    /**
     * 获取角色请所有菜单
     *
     * @param roleId 角色主键id
     * @return 菜单列表
     */
    List<SysMenuVO> listByRoleId(BigInteger roleId);

    /**
     * 缓存用户权限
     *
     * @param userId 用户主键id
     * @return 用户权限
     */
    List<SysMenuVO> cachePermission(BigInteger userId);

    /**
     * 获取用户缓存权限
     *
     * @param userId 用户主键id
     * @return 用户权限
     */
    List<SysMenuVO> getCachePermission(BigInteger userId);

    /**
     * 通关角色获取菜单下拉选项
     *
     * @param pageDTO {@linkplain SysMenuTreeDTO}
     * @return {@link SysMenuVO}
     */
    List<Tree<BigInteger>> listByTreeAsRoleSelection(SysMenuTreeDTO pageDTO);

    /**
     * 获取用户菜单
     *
     * @return 菜单列表
     */
    List<Tree<BigInteger>> userMenu();

}
