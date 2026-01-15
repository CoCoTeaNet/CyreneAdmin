package net.cocotea.cyreneadmin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.cocotea.cyreneadmin.model.dto.SysLoginUserUpdateDTO;
import net.cocotea.cyreneadmin.model.dto.SysUserAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysUserPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysUserUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysUserVO;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysUserService;
import net.cocotea.cyreneadmin.annotation.LogPersistence;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 系统用户管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@RequestMapping("/system/user")
@RestController
public class SysUserController {

    @Autowired
    private AppSystemProp appSystemProp;

    @Resource
    private SysUserService userService;

    /**
     * 新增用户
     *
     * @param addDTO {@link SysUserAddDTO}
     * @return 成功返回TRUE
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public ApiResult<Boolean> add(@Valid @RequestBody SysUserAddDTO addDTO) throws BusinessException {
        boolean b = userService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO {@link SysUserUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public ApiResult<Boolean> update(@Valid @RequestBody SysUserUpdateDTO updateDTO) throws BusinessException {
        boolean b = userService.update(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 成功返回TRUE
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/delete/{id}")
    public ApiResult<Boolean> delete(@PathVariable("id") BigInteger id) throws BusinessException {
        boolean b = userService.delete(id);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除用户
     *
     * @param idList 用户ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@RequestBody List<BigInteger> idList) throws BusinessException {
        boolean b = userService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询用户
     *
     * @param pageDTO {@link SysUserPageDTO}
     * @return {@link ApiPage<SysUserVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/listByPage")
    public ApiResult<ApiPage<SysUserVO>> listByPage(@Valid @RequestBody SysUserPageDTO pageDTO) throws BusinessException {
        ApiPage<SysUserVO> list = userService.listByPage(pageDTO);
        return ApiResult.ok(list);
    }

    /**
     * 获取用户详细
     *
     * @return {@link SysUserVO}
     */
    @GetMapping("/getDetail")
    public ApiResult<SysUserVO> getDetail() {
        SysUserVO vo = userService.getDetail();
        return ApiResult.ok(vo);
    }

    /**
     * 更新登录用户信息
     *
     * @param updateDTO {@link SysLoginUserUpdateDTO}
     * @return 成功返回TRUE
     */
    @PostMapping("/updateByUser")
    public ApiResult<Boolean> updateByUser(@Valid @RequestBody SysLoginUserUpdateDTO updateDTO) {
        boolean b = userService.updateByUser(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 修改用户密码
     *
     * @param obj oldPassword:旧密码，newPassword:新密码
     * @return 成功返回TRUE
     */
    @PostMapping("/doModifyPassword")
    public ApiResult<Boolean> doModifyPassword(@RequestBody JSONObject obj) throws BusinessException {
        boolean r = userService.doModifyPassword(obj.getString("oldPassword"), obj.getString("newPassword"));
        return ApiResult.ok(r);
    }

    /**
     * 获取用户头像
     *
     * @param avatar 头像名称
     * @param response 响应
     * @throws BusinessException 业务异常
     * @throws IOException IO异常
     */
    @GetMapping("getAvatar")
    public void getAvatar(@RequestParam("avatar") String avatar, HttpServletResponse response) throws BusinessException, IOException {
        userService.getAvatar(avatar, response.getOutputStream());
    }

    /**
     * 系统用户头像上传
     *
     * @param multipartFile {@link MultipartFile}
     * @return 成功返回 true
     */
    @PostMapping("/avatar/upload")
    public ApiResult<Boolean> uploadAvatar(@RequestParam("file") MultipartFile multipartFile) throws BusinessException, IOException {
        if (StrUtil.isBlank(appSystemProp.getAvatarPath())) {
            throw new BusinessException("未配置相关信息");
        }
        FileUploadUtils.filter(multipartFile.getOriginalFilename(), appSystemProp.getSupportFiletype());
        String saveName = IdUtil.objectId() + CharPool.UNDERLINE + multipartFile.getOriginalFilename();
        String fullPath = appSystemProp.getAvatarPath() + saveName;
        File file = new File(fullPath);
        if (!file.exists()) {
            FileUtil.mkdir(appSystemProp.getAvatarPath());
        }
        multipartFile.transferTo(file);
        FileUploadUtils.validAvatar(file);
        userService.doModifyAvatar(saveName);
        return ApiResult.ok(true);
    }

}
