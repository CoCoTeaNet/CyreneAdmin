package net.cocotea.cyreneadmin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
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
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * 系统用户管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Mapping("/system/user")
@Controller
@Valid
public class SysUserController {

    @Inject
    private AppSystemProp appSystemProp;

    @Inject
    private SysUserService sysUserService;

    /**
     * 新增用户
     *
     * @param addDTO {@link SysUserAddDTO}
     * @return 成功返回TRUE
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/add")
    public ApiResult<Boolean> add(@Validated @Body SysUserAddDTO addDTO) throws BusinessException {
        boolean b = sysUserService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO {@link SysUserUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/update")
    public ApiResult<Boolean> update(@Validated @Body SysUserUpdateDTO updateDTO) throws BusinessException {
        boolean b = sysUserService.update(updateDTO);
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
    @Post @Mapping("/delete/{id}")
    public ApiResult<Boolean> delete(@Path BigInteger id) throws BusinessException {
        boolean b = sysUserService.delete(id);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除用户
     *
     * @param idList 用户ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Body List<BigInteger> idList) throws BusinessException {
        boolean b = sysUserService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询用户
     *
     * @param pageDTO {@link SysUserPageDTO}
     * @return {@link ApiPage<SysUserVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/listByPage")
    public ApiResult<ApiPage<SysUserVO>> listByPage(@Validated @Body SysUserPageDTO pageDTO) throws BusinessException {
        ApiPage<SysUserVO> list = sysUserService.listByPage(pageDTO);
        return ApiResult.ok(list);
    }

    /**
     * 获取用户详细
     *
     * @return {@link SysUserVO}
     */
    @Get @Mapping("/getDetail")
    public ApiResult<SysUserVO> getDetail() {
        SysUserVO vo = sysUserService.getDetail();
        return ApiResult.ok(vo);
    }

    /**
     * 更新登录用户信息
     *
     * @param updateDTO {@link SysLoginUserUpdateDTO}
     * @return 成功返回TRUE
     */
    @Post @Mapping("/updateByUser")
    public ApiResult<Boolean> updateByUser(@Validated @Body SysLoginUserUpdateDTO updateDTO) {
        boolean b = sysUserService.updateByUser(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 修改用户密码
     *
     * @param obj oldPassword:旧密码，newPassword:新密码
     * @return 成功返回TRUE
     */
    @Post @Mapping("/doModifyPassword")
    public ApiResult<Boolean> doModifyPassword(@Body JSONObject obj) throws BusinessException {
        boolean r = sysUserService.doModifyPassword(obj.getString("oldPassword"), obj.getString("newPassword"));
        return ApiResult.ok(r);
    }

    /**
     * 系统用户头像文件获取
     *
     * @param avatar 头像文件名称
     */
    @Get
    @Mapping("/getAvatar")
    public void getAvatar(@Param("avatar") String avatar, Context context) throws BusinessException, IOException {
        sysUserService.getAvatar(avatar, context.outputStream());
    }

    /**
     * 系统用户头像上传
     *
     * @param uploadedFile {@link UploadedFile}
     * @return 成功返回 true
     */
    @Post
    @Mapping("/avatar/upload")
    public ApiResult<Boolean> uploadAvatar(@Param("file") UploadedFile uploadedFile) throws BusinessException, IOException {
        FileUploadUtils.filter(uploadedFile.getName(), appSystemProp.getSupportFiletype());
        String saveName = IdUtil.objectId() + CharPool.UNDERLINE + uploadedFile.getName();
        String fullPath = appSystemProp.getAvatarPath() + saveName;
        File file = new File(fullPath);
        if (!file.exists()) {
            FileUtil.mkdir(appSystemProp.getAvatarPath());
        }
        uploadedFile.transferTo(file);
        FileUploadUtils.validAvatar(file);
        sysUserService.doModifyAvatar(saveName);
        return ApiResult.ok(true);
    }

}
