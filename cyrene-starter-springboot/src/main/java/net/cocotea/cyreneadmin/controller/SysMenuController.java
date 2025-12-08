package net.cocotea.cyreneadmin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.cocotea.cyreneadmin.model.dto.SysMenuAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuTreeDTO;
import net.cocotea.cyreneadmin.model.dto.SysMenuUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysMenuVO;
import net.cocotea.cyreneadmin.service.SysMenuService;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统菜单或权限管理接口
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {
    @Resource
    private SysMenuService sysMenuService;

    /**
     * 新增菜单或权限
     *
     * @param addDTO {@link SysMenuAddDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("add")
    public ApiResult<Boolean> add(@Valid @RequestBody SysMenuAddDTO addDTO) throws BusinessException {
        boolean b = sysMenuService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除菜单或权限
     *
     * @param idList ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Valid @RequestBody List<BigInteger> idList) throws BusinessException {
        boolean b = sysMenuService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 更新菜单或权限信息
     *
     * @param updateDTO {@link SysMenuUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("update")
    public ApiResult<Boolean> update(@Valid @RequestBody SysMenuUpdateDTO updateDTO) throws BusinessException {
        boolean b = sysMenuService.update(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询菜单或权限
     *
     * @param pageDTO {@link SysMenuPageDTO}
     * @return {@link ApiPage<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("listByPage")
    public ApiResult<ApiPage<SysMenuVO>> listByPage(@Valid @RequestBody SysMenuPageDTO pageDTO) throws BusinessException {
        ApiPage<SysMenuVO> menus = sysMenuService.listByPage(pageDTO);
        return ApiResult.ok(menus);
    }

    /**
     * 查询菜单或权限树形数据
     *
     * @param pageDTO {@link SysMenuPageDTO}
     * @return {@link List<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("listByTree")
    public ApiResult<List<SysMenuVO>> listByTree(@Valid @RequestBody SysMenuTreeDTO pageDTO) {
        List<SysMenuVO> menus = sysMenuService.listByTree(pageDTO);
        return ApiResult.ok(menus);
    }

    /**
     * 通关角色获取菜单或权限
     *
     * @param pageDTO {@link SysMenuPageDTO}
     * @return {@link List<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("listByTreeAsRoleSelection")
    public ApiResult<List<SysMenuVO>> listByTreeAsRoleSelection(@Valid @RequestBody SysMenuTreeDTO pageDTO) {
        List<SysMenuVO> menus = sysMenuService.listByTreeAsRoleSelection(pageDTO);
        return ApiResult.ok(menus);
    }

    /**
     * 通关角色获取请所有菜单
     *
     * @param roleId 角色ID
     * @return {@link List<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @GetMapping("listByRoleId/{roleId}")
    public ApiResult<List<SysMenuVO>> listByRoleId(@PathVariable("roleId") String roleId) {
        List<SysMenuVO> menus = sysMenuService.listByRoleId(roleId);
        return ApiResult.ok(menus);
    }
}
