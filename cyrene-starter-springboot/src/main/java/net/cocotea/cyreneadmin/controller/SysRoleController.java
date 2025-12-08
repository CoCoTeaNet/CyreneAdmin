package net.cocotea.cyreneadmin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.cocotea.cyreneadmin.model.dto.SysRoleAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysRolePageDTO;
import net.cocotea.cyreneadmin.model.dto.SysRoleUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysRoleMenuVO;
import net.cocotea.cyreneadmin.model.vo.SysRoleVO;
import net.cocotea.cyreneadmin.service.SysRoleService;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统角色管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@RequestMapping("/system/role")
@RestController
public class SysRoleController {
    @Resource
    private SysRoleService sysRoleService;

    /**
     * 新增角色
     *
     * @param addDTO {@link SysRoleAddDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public ApiResult<Boolean> add(@Valid @RequestBody SysRoleAddDTO addDTO) throws BusinessException {
        boolean b = sysRoleService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 更新角色信息
     *
     * @param updateDTO {@link SysRoleUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public ApiResult<Boolean> update(@Valid @RequestBody SysRoleUpdateDTO updateDTO) throws BusinessException {
        boolean b = sysRoleService.update(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/delete/{id}")
    public ApiResult<Boolean> delete(@PathVariable("id") BigInteger id) throws BusinessException {
        boolean b = sysRoleService.delete(id);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除角色
     *
     * @param idList 角色ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@RequestBody List<BigInteger> idList) throws BusinessException {
        boolean b = sysRoleService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 给角色赋予权限
     *
     * @param updateDTO {@link SysRoleMenuVO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/grantPermissionsByRoleId")
    public ApiResult<Boolean> grantPermissionsByRoleId(@Valid @RequestBody List<SysRoleMenuVO> updateDTO) throws BusinessException {
        boolean b = sysRoleService.grantPermissionsByRoleId(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询角色
     *
     * @param pageDTO {@link SysRolePageDTO}
     * @return {@link ApiPage<SysRoleVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/listByPage")
    public ApiResult<ApiPage<SysRoleVO>> listByPage(@Valid @RequestBody SysRolePageDTO pageDTO) throws BusinessException {
        ApiPage<SysRoleVO> p = sysRoleService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
