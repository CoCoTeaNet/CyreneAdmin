package net.cocotea.cyreneadmin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.*;
import net.cocotea.cyreneadmin.model.po.SysUser;
import net.cocotea.cyreneadmin.model.po.SysUserRole;
import net.cocotea.cyreneadmin.model.vo.*;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysUserService;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.TreeBuilder;
import net.cocotea.cyreneadmin.util.LoginUtils;
import net.cocotea.cyreneadmin.util.SecurityUtils;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.utils.StringUtil;

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

    @Override
    public boolean add(SysUserAddDTO addDTO) {
        SysUser sysUser = lightDao.convertType(addDTO, SysUser.class);
        if (StringUtil.isNotBlank(addDTO.getPassword())) {
            sysUser.setPassword(securityUtils.getPwd(addDTO.getPassword()));
        } else {
            sysUser.setPassword(appSystemProp.getPassword());
        }
        Object userId = lightDao.save(sysUser);

        // 授予用户角色
        if (!(addDTO.getRoleIds().isEmpty())) {
            for (BigInteger roleId : addDTO.getRoleIds()) {
                SysUserRole sysUserRole = new SysUserRole().setUserId(LoginUtils.parse(userId)).setRoleId(roleId);
                lightDao.save(sysUserRole);
            }
        }

        return userId != null;
    }

    @Override
    public boolean delete(BigInteger id) {
        // 假删除，用户关联的数据不必操作
        SysUser sysUser = new SysUser().setId(id).setIsDeleted(IsEnum.Y.getCode());
        return lightDao.update(sysUser) > 0;
    }

    @Override
    public boolean update(SysUserUpdateDTO updateDTO) {
        SysUser sysUser = Convert.convert(SysUser.class, updateDTO);
        if (!(updateDTO.getRoleIds() == null || updateDTO.getRoleIds().isEmpty())) {
            // 删除用户角色关联
            EntityQuery sysUserRoleQuery = EntityQuery.create().where("#[user_id = :userId]").names("userId").values(updateDTO.getId());
            lightDao.deleteByQuery(SysUserRole.class, sysUserRoleQuery);
            // 添加用户角色关联
            for (BigInteger roleId : updateDTO.getRoleIds()) {
                SysUserRole sysUserRole = new SysUserRole().setUserId(updateDTO.getId()).setRoleId(roleId);
                lightDao.save(sysUserRole);
            }
        }
        // 更新密码
        if (StringUtil.isNotBlank(updateDTO.getPassword())) {
            sysUser.setPassword(securityUtils.getPwd(updateDTO.getPassword()));
        }
        Long flag = lightDao.update(sysUser);
        return flag > 0;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        if (idList != null) {
            idList.forEach(this::delete);
        }
        return idList != null && !idList.isEmpty();
    }

    @Override
    public ApiPage<SysUserVO> listByPage(SysUserPageDTO pageDTO) {
        Map<String, Object> params = BeanUtil.beanToMap(pageDTO.getSysUser());

        Page<SysUserVO> page = lightDao.findPage(ApiPage.create(pageDTO), "sys_user_findList", params, SysUserVO.class);

        List<BigInteger> userIds = page.getRows().stream().map(SysUserVO::getId).toList();
        Map<BigInteger, List<SysUserRoleVO>> roleMap = userRoleMap(userIds);

        page.getRows().forEach(row -> row.setRoleList(roleMap.get(row.getId())));
        return ApiPage.rest(page);
    }

    @Override
    public String login(SysLoginDTO loginDTO, String realIp) throws BusinessException {
        SysUser sysUser;

        // 强密码为空或者为none表示“不启用”
        boolean isStrongPwd = !(StrUtil.isBlank(appSystemProp.getStrongPassword()) || "none".equals(appSystemProp.getStrongPassword()));
        if (isStrongPwd) {
            boolean pwdValid = Objects.equals(appSystemProp.getStrongPassword(), loginDTO.getPassword());
            Assert.isTrue(pwdValid, () -> new BusinessException("密码不正确"));
        }

        // 验证码缓存键
        String key = null;
        if (!isStrongPwd) {
            // 校验验证码
            key = String.format(RedisKeyConst.VERIFY_CODE_LOGIN, loginDTO.getCaptchaId());
            String code = redisService.get(key);
            if (!loginDTO.getCaptcha().equals(code)) {
                throw new BusinessException("验证码错误");
            }
            // 校验密码
            String pwd = securityUtils.getPwd(loginDTO.getPassword());
            sysUser = lightDao.findOne("sys_user_getOne", new SysUser().setUsername(loginDTO.getUsername()).setPassword(pwd), SysUser.class);
            if (sysUser == null) {
                throw new BusinessException("登录失败，用户名或密码错误");
            }
        } else {
            sysUser = lightDao.findOne("sys_user_getOne", new SysUser().setUsername(loginDTO.getUsername()), SysUser.class);
        }
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
        // 删除缓存
        if (StrUtil.isNotBlank(key)) {
            redisService.delete(key);
        }
        return StpUtil.getTokenValue();
    }

    @Override
    public SysUserVO getDetail() {
        BigInteger loginId = LoginUtils.loginId();
        // 用户信息
        SysUser sysUser = lightDao.findOne("sys_user_getOne", new SysUser().setId(loginId), SysUser.class);
        SysUserVO sysUserVO = Convert.convert(SysUserVO.class, sysUser);
        // 用户角色
        Map<BigInteger, List<SysUserRoleVO>> roleMap = userRoleMap(Collections.singletonList(loginId));
        return sysUserVO.setRoleList(roleMap.get(loginId));
    }

    @Override
    public SysLoginUserVO loginUser() {
        BigInteger loginId = LoginUtils.loginIdEx();
        SysUser sysUser = lightDao.findOne("sys_user_getOne", new SysUser().setId(loginId), SysUser.class);
        SysLoginUserVO sysLoginUser = new SysLoginUserVO();
        // 用户菜单
        List<SysMenuTreeVO> userMenus = userMenu(loginId);
        List<SysMenuTreeVO> menuList = new TreeBuilder<SysMenuTreeVO>().get(userMenus);
        sysLoginUser.setMenuList(menuList);
        // 用户基本信息
        sysLoginUser.setId(sysUser.getId())
                .setUsername(sysUser.getUsername())
                .setNickname(sysUser.getNickname())
                .setAvatar(sysUser.getAvatar())
                .setLoginStatus(true)
                .setToken(StpUtil.getTokenValue());
        return sysLoginUser;
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
        SysUser sysUser = lightDao.findOne("sys_user_getOne", new SysUser().setId(loginId), SysUser.class);
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
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("ids", ids);
        List<SysUser> list = lightDao.find("sys_user_findList", map, SysUser.class);
        return list.stream().collect(Collectors.toMap(SysUser::getId, i -> i));
    }

    @Override
    public void doModifyAvatar(String avatarName) {
        BigInteger loginId = LoginUtils.loginId();
        SysUser sysUser = new SysUser().setId(loginId).setAvatar(avatarName);
        lightDao.update(sysUser);
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

    private List<SysMenuTreeVO> userMenu(BigInteger userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("userIds", List.of(userId))
                .build();
        return lightDao.find("sys_menu_findUserMenu", params, SysMenuTreeVO.class);
    }

}
