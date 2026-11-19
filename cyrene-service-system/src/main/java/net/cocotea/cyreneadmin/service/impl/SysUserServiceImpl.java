package net.cocotea.cyreneadmin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.*;
import net.cocotea.cyreneadmin.model.po.SysUser;
import net.cocotea.cyreneadmin.model.po.SysUserRole;
import net.cocotea.cyreneadmin.model.vo.*;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysRoleService;
import net.cocotea.cyreneadmin.service.SysUserService;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.LoginUtils;
import net.cocotea.cyreneadmin.util.SecurityUtils;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.lambda.LambdaDelete;
import org.sagacity.sqltoy.lambda.LambdaQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.utils.StringUtil;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.OutputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final AppSystemProp appSystemProp;
    private final RedisService redisService;
    private final SecurityUtils securityUtils;
    private final LightDao lightDao;
    private final SysRoleService roleService;

    @Tran
    @Transactional
    @Override
    public boolean add(SysUserAddDTO addDTO) {
        checkAssignableRoles(addDTO.getRoleIds(), null);
        SysUser existUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getUsername, addDTO.getUsername())
                .getOne();
        if (existUser != null) {
            throw new BusinessException("已存在该用户名");
        }
        SysUser sysUser = lightDao.convertType(addDTO, SysUser.class);
        // 未填密码时使用默认密码，同样走 SM3+salt，避免明文落库
        String rawPwd = StringUtil.isNotBlank(addDTO.getPassword()) ? addDTO.getPassword() : appSystemProp.getPassword();
        sysUser.setPassword(securityUtils.getPwd(rawPwd));
        Object userId = lightDao.save(sysUser);

        // 授予用户角色
        if (addDTO.getRoleIds() != null && !addDTO.getRoleIds().isEmpty()) {
            for (BigInteger roleId : addDTO.getRoleIds()) {
                SysUserRole sysUserRole = new SysUserRole().setUserId(LoginUtils.parse(userId)).setRoleId(roleId);
                lightDao.save(sysUserRole);
            }
        }

        return userId != null;
    }

    @Override
    public boolean delete(BigInteger id) {
        checkAssignableRoles(null, id);
        // 假删除，用户关联的数据不必操作
        SysUser sysUser = new SysUser().setId(id).setIsDeleted(IsEnum.Y.getCode());
        // 删除后权限缓存立即失效，避免残留会话继续使用旧权限
        redisService.delete(String.format(RedisKeyConst.USER_PERMISSION, id));
        boolean result = lightDao.update(sysUser) > 0;
        // 删除后强制下线，已建立的会话不能继续使用
        if (result) {
            StpUtil.kickout(id);
        }
        return result;
    }

    @Tran
    @Transactional
    @Override
    public boolean update(SysUserUpdateDTO updateDTO) {
        checkAssignableRoles(updateDTO.getRoleIds(), updateDTO.getId());
        SysUser existUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getUsername, updateDTO.getUsername())
                .ne(true, SysUser::getId, updateDTO.getId())
                .getOne();
        if (existUser != null) {
            throw new BusinessException("已存在该用户名");
        }
        SysUser sysUser = Convert.convert(SysUser.class, updateDTO);
        if (updateDTO.getRoleIds() != null && !updateDTO.getRoleIds().isEmpty()) {
            // 删除用户角色关联
            LambdaDelete.create(SysUserRole.class).bind(lightDao)
                    .eq(true, SysUserRole::getUserId, updateDTO.getId())
                    .delete();
            // 添加用户角色关联
            for (BigInteger roleId : updateDTO.getRoleIds()) {
                SysUserRole sysUserRole = new SysUserRole().setUserId(updateDTO.getId()).setRoleId(roleId);
                lightDao.save(sysUserRole);
            }
            // 角色变化后清理该用户权限缓存
            redisService.delete(String.format(RedisKeyConst.USER_PERMISSION, updateDTO.getId()));
        }
        // 更新密码
        if (StringUtil.isNotBlank(updateDTO.getPassword())) {
            sysUser.setPassword(securityUtils.getPwd(updateDTO.getPassword()));
        }
        Long flag = lightDao.update(sysUser);
        // 停用或冻结后立即踢出会话，避免旧 token 继续访问其他接口
        if (flag > 0 && !StringUtil.isBlank(updateDTO.getAccountStatus())
                && !"1".equals(updateDTO.getAccountStatus())) {
            StpUtil.kickout(updateDTO.getId());
        }
        return flag > 0;
    }

    /**
     * 非超管只能操作角色范围不超过自身的用户，且只能分配自身已拥有的角色、不能调整自己的角色
     */
    private void checkAssignableRoles(List<BigInteger> roleIds, BigInteger targetUserId) {
        BigInteger operatorId = LoginUtils.loginId();
        if (operatorId == null || roleService.isSuperAdmin(operatorId)) {
            return;
        }
        boolean self = targetUserId != null && targetUserId.equals(operatorId);
        Set<BigInteger> ownRoleIds = null;
        if (targetUserId != null && !self) {
            ownRoleIds = roleIdsOf(operatorId);
            // 防止改密/删除高权限账号，目标用户角色必须是操作者角色的子集
            if (!ownRoleIds.containsAll(roleIdsOf(targetUserId))) {
                throw new BusinessException("无权操作该用户");
            }
        }
        // 未提交角色表示不调整角色
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        if (self) {
            if (!roleIdsOf(operatorId).equals(new HashSet<>(roleIds))) {
                throw new BusinessException("不能修改自己的角色");
            }
            return;
        }
        if (ownRoleIds == null) {
            ownRoleIds = roleIdsOf(operatorId);
        }
        if (!ownRoleIds.containsAll(roleIds)) {
            throw new BusinessException("无权分配超出自身权限的角色");
        }
    }

    private Set<BigInteger> roleIdsOf(BigInteger userId) {
        return LambdaQuery.create(SysUserRole.class).bind(lightDao)
                .eq(true, SysUserRole::getUserId, userId)
                .find().stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        if (idList != null && !idList.isEmpty()) {
            // 先整体校验，避免批量删除中途失败留下部分数据
            idList.forEach(id -> checkAssignableRoles(null, id));
            idList.forEach(this::delete);
        }
        return idList != null && !idList.isEmpty();
    }

    @Override
    public ApiPage<SysUserVO> listByPage(SysUserPageDTO pageDTO) {
        SysUserPageDTO.Query query = pageDTO.getSysUser();
        Page<SysUser> page = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .like(true, SysUser::getUsername, query.getUsername())
                .like(true, SysUser::getNickname, query.getNickname())
                .like(true, SysUser::getEmail, query.getEmail())
                .eq(true, SysUser::getSex, query.getSex())
                .eq(true, SysUser::getMobilePhone, query.getMobilePhone())
                .eq(true, SysUser::getAccountStatus, query.getAccountStatus())
                .orderBy(true, false, SysUser::getId)
                .findPage(ApiPage.create(pageDTO));

        List<SysUserVO> rows = BeanUtil.copyToList(page.getRows(), SysUserVO.class);
        List<BigInteger> userIds = rows.stream().map(SysUserVO::getId).toList();
        Map<BigInteger, List<SysUserRoleVO>> roleMap = userRoleMap(userIds);

        rows.forEach(row -> row.setRoleList(roleMap.get(row.getId())));
        return ApiPage.rest(page, rows);
    }

    @Override
    public String login(SysLoginDTO loginDTO, String realIp) throws BusinessException {
        // 校验验证码
        String key = String.format(RedisKeyConst.VERIFY_CODE_LOGIN, loginDTO.getCaptchaId());
        String code = redisService.get(key);
        if (code == null || !code.equals(loginDTO.getCaptcha())) {
            throw new BusinessException("验证码错误");
        }
        // 验证码一次性使用，避免同一验证码被重复用于撞库
        redisService.delete(key);
        // 校验密码
        String pwd = securityUtils.getPwd(loginDTO.getPassword());
        SysUser sysUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysUser::getUsername, loginDTO.getUsername())
                .eq(true, SysUser::getPassword, pwd)
                .orderBy(true, false, SysUser::getId)
                .getOne();
        if (sysUser == null) {
            throw new BusinessException("登录失败，用户名或密码错误");
        }
        // 账号状态字典 account_status：仅 1=正常 允许登录
        checkAccountStatus(sysUser.getAccountStatus(), false);
        // 记住我模式
        if (loginDTO.getRememberMe()) {
            StpUtil.login(sysUser.getId(), new SaLoginParameter().setTimeout(3600 * 24 * 365));
        } else {
            StpUtil.login(sysUser.getId());
        }
        // 更新用户登录时间和ip
        SysUser loginSysUser = new SysUser();
        loginSysUser.setId(sysUser.getId());
        loginSysUser.setLastLoginIp(realIp);
        loginSysUser.setLastLoginTime(LocalDateTime.now());
        lightDao.update(loginSysUser);
        return StpUtil.getTokenValue();
    }

    @Override
    public SysUserVO getDetail() {
        BigInteger loginId = LoginUtils.loginId();
        // 用户信息
        SysUser sysUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysUser::getId, loginId)
                .getOne();
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        SysUserVO sysUserVO = Convert.convert(SysUserVO.class, sysUser);
        // 用户角色
        Map<BigInteger, List<SysUserRoleVO>> roleMap = userRoleMap(Collections.singletonList(loginId));
        return sysUserVO.setRoleList(roleMap.get(loginId));
    }

    @Override
    public SysLoginUserVO loginUser() {
        BigInteger loginId = LoginUtils.loginIdEx();
        SysUser sysUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysUser::getId, loginId)
                .getOne();
        // 强制退出登录
        if (sysUser == null) {
            StpUtil.logout();
            throw new BusinessException("用户已退出登录");
        }
        // 账号被停用/冻结/封禁后，已登录会话同样失效
        checkAccountStatus(sysUser.getAccountStatus(), true);
        return new SysLoginUserVO().setId(sysUser.getId())
                .setUsername(sysUser.getUsername())
                .setNickname(sysUser.getNickname())
                .setAvatar(sysUser.getAvatar())
                .setLoginStatus(true)
                .setToken(StpUtil.getTokenValue());
    }

    @Override
    public boolean updateByUser(SysLoginUserUpdateDTO param) {
        SysUser sysUser = Convert.convert(SysUser.class, param);
        sysUser.setId(LoginUtils.loginId());
        return lightDao.update(sysUser) > 0;
    }

    @Override
    public boolean doModifyPassword(String oldPassword, String newPassword) throws BusinessException {
        if (StringUtil.isBlank(oldPassword)) {
            throw new BusinessException("旧密码为空");
        }
        if (StringUtil.isBlank(newPassword)) {
            throw new BusinessException("新密码为空");
        }
        BigInteger loginId = LoginUtils.loginId();
        SysUser sysUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysUser::getId, loginId)
                .getOne();
        String pwdOld = securityUtils.getPwd(oldPassword);
        if (!sysUser.getPassword().equals(pwdOld)) {
            throw new BusinessException("旧密码不正确");
        }
        String pwdNew = securityUtils.getPwd(newPassword);
        sysUser.setPassword(pwdNew);
        return lightDao.update(sysUser) > 0;
    }

    @Override
    public Map<BigInteger, SysUser> getMap(List<BigInteger> ids) {
        if (ids == null || ids.isEmpty()) {
            return MapUtil.empty();
        }
        List<SysUser> list = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .in(true, SysUser::getId, ids)
                .orderBy(true, false, SysUser::getId)
                .find();
        return list.stream().collect(Collectors.toMap(SysUser::getId, i -> i));
    }

    @Override
    public void doModifyAvatar(String avatarName) {
        BigInteger loginId = LoginUtils.loginId();
        SysUser sysUser = new SysUser().setId(loginId).setAvatar(avatarName);
        lightDao.update(sysUser);
    }

    @Override
    public void getAvatar(String avatar, OutputStream outputStream) throws BusinessException {
        // 头像名只允许文件名，禁止路径分隔符与上级目录，避免目录穿越
        if (StringUtil.isBlank(avatar)
                || avatar.contains("..")
                || avatar.contains("/")
                || avatar.contains("\\")) {
            throw new BusinessException("非法头像名称");
        }
        String fullPath = appSystemProp.getAvatarPath() + avatar;
        File file = FileUtil.file(fullPath);
        if (file.exists()) {
            FileUtil.writeToStream(file, outputStream);
        } else {
            throw new BusinessException("File is not found");
        }
    }

    /**
     * 校验账号状态，仅 1=正常 放行
     *
     * @param accountStatus 账号状态
     * @param logout        不通过时是否注销当前会话
     */
    private void checkAccountStatus(Integer accountStatus, boolean logout) {
        if (Integer.valueOf(1).equals(accountStatus)) {
            return;
        }
        if (logout) {
            StpUtil.logout();
        }
        String label = switch (accountStatus == null ? -1 : accountStatus) {
            case 0 -> "已停用";
            case 2 -> "已冻结";
            case 3 -> "已封禁";
            default -> "未知";
        };
        throw new BusinessException("账号状态异常（" + label + "），禁止登录");
    }

    private Map<BigInteger, List<SysUserRoleVO>> userRoleMap(List<BigInteger> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return MapUtil.empty();
        }
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("userIds", userIds)
                .build();
        return lightDao.find("sys_role_findUserRole", params, SysUserRoleVO.class)
                .stream()
                .collect(groupingBy(SysUserRole::getUserId));
    }

}
