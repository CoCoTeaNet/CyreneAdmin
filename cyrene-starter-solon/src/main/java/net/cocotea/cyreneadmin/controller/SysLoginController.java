package net.cocotea.cyreneadmin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.dto.SysLoginDTO;
import net.cocotea.cyreneadmin.model.vo.SysCaptchaVO;
import net.cocotea.cyreneadmin.model.vo.SysLoginUserVO;
import net.cocotea.cyreneadmin.service.SysLogService;
import net.cocotea.cyreneadmin.service.SysMenuService;
import net.cocotea.cyreneadmin.service.SysUserService;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.LogTypeEnum;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.service.RedisService;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

/**
 * 系统登录相关接口
 *
 * @author CoCoTea
 */
@Slf4j
@Controller
@Mapping("/system")
@Valid
public class SysLoginController {

    @Inject
    private SysUserService sysUserService;

    @Inject
    private SysMenuService sysMenuService;

    @Inject
    private RedisService redisService;

    @Inject
    private SysLogService sysLogService;

    /**
     * 后台系统用户登录
     *
     * @param loginDTO {@link SysLoginDTO}
     * @param context  {@link Context}
     * @return token
     */
    @Post
    @Mapping("/login")
    public ApiResult<String> login(@Validated @Body SysLoginDTO loginDTO, Context context) throws BusinessException {
        // 获取缓存密钥对
        String key = String.format(RedisKeyConst.SM2_KEY_LOGIN, loginDTO.getPublicKey());
        String privateKey = redisService.get(key);
        if (StrUtil.isBlank(privateKey)) {
            throw new BusinessException("验证码已过期");
        }
        // 对密码解密操作
        SM2 sm2 = SmUtil.sm2(privateKey, loginDTO.getPublicKey());
        String decryptPassword = StrUtil.utf8Str(sm2.decrypt(loginDTO.getPassword(), KeyType.PrivateKey));
        loginDTO.setPassword(decryptPassword);
        // 开始登录逻辑
        String token = sysUserService.login(loginDTO, context.realIp());
        // 删除缓存
        redisService.delete(key);
        // 保存登录日志
        sysLogService.saveByLogType(LogTypeEnum.LOGIN.getCode(), context.realIp(), context.method());
        return ApiResult.ok(token);
    }

    /**
     * 后台系统用户退出登录
     *
     * @return {@link ApiResult}
     */
    @Post
    @Mapping("/logout")
    public ApiResult<?> logout() {
        // 删除权限缓存
        redisService.delete(String.format(RedisKeyConst.USER_PERMISSION, StpUtil.getLoginId()));
        redisService.delete(String.format(RedisKeyConst.ONLINE_USER, StpUtil.getLoginId()));
        StpUtil.logout();
        return ApiResult.ok();
    }

    /**
     * 获取用户登录信息
     *
     * @return {@link SysLoginUserVO}
     */
    @Get
    @Mapping("/loginInfo")
    public ApiResult<SysLoginUserVO> loginInfo() {
        SysLoginUserVO r = sysUserService.loginUser();
        return ApiResult.ok(r);
    }

    /**
     * 获取用户菜单
     *
     * @return {@link net.cocotea.cyreneadmin.model.vo.SysMenuTreeVO}
     */
    @Get
    @Mapping("/user/menus")
    public ApiResult<List<Tree<BigInteger>>> userMenu() {
        List<Tree<BigInteger>> menus = sysMenuService.userMenu();
        return ApiResult.ok(menus);
    }

    /**
     * 获取后台登录验证码
     *
     * @param timestamp 时间戳
     * @return {@link SysCaptchaVO}
     */
    @Get
    @Mapping("/captcha")
    public ApiResult<SysCaptchaVO> captcha(@Param("timestamp") String timestamp, Context context) {
        log.info("captcha >>>>> timestamp: {}", timestamp);
        long cacheSeconds = 300L;
        // 生成验证码ID
        String captchaId = IdUtil.fastUUID().toUpperCase(Locale.ROOT);
        // 生成圆圈干扰的验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 100, 4, 20);
        redisService.save(
                String.format(RedisKeyConst.VERIFY_CODE_LOGIN, captchaId),
                captcha.getCode(),
                cacheSeconds
        );
        // 生成公钥（加密）和私钥（解密）
        SM2 sm2 = SmUtil.sm2();
        String privateKey = sm2.getPrivateKeyBase64();
        String publicKey = HexUtil.encodeHexStr(((BCECPublicKey) sm2.getPublicKey()).getQ().getEncoded(false));
        // 保存密钥对
        redisService.save(
                String.format(RedisKeyConst.SM2_KEY_LOGIN, publicKey),
                privateKey,
                cacheSeconds
        );
        // 登录验证码对象
        SysCaptchaVO captchaVO = new SysCaptchaVO()
                .setCaptchaId(captchaId)
                .setImgBase64(captcha.getImageBase64())
                .setPublicKey(publicKey);
        return ApiResult.ok(captchaVO);
    }
}
