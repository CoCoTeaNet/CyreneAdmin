package net.cocotea.cyreneadmin.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.dto.SysLogAddDTO;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysLogService;
import net.cocotea.cyreneadmin.annotation.LogPersistence;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.ApiResultEnum;
import net.cocotea.cyreneadmin.enums.LogStatusEnum;
import net.cocotea.cyreneadmin.enums.LogTypeEnum;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.model.NotLogException;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.exception.StatusException;
import org.noear.solon.core.handle.*;

import java.math.BigInteger;

@Slf4j
@Component
public class AppFilter implements Filter {
    
    @Inject
    private AppSystemProp appSystemProp;

    @Inject
    private SysLogService sysLogService;

    @Inject
    private RedisService redisService;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        //1.开始计时（用于计算响应时长）
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(ctx);

            //2.未处理设为404状态
            if (!ctx.getHandled()) {
                ctx.status(404);
            }

            if (ctx.status() == 404) {
                ApiResult<Object> result = new ApiResult<>(ApiResultEnum.NOT_FOUNT.getCode(), null, ApiResultEnum.NOT_FOUNT.getDesc());
                ctx.render(result);
            }

            onlineUsersRenewal();
        } catch (Exception e) {
            ctx.status(ApiResultEnum.SUCCESS.getCode());
            //4.异常捕促与控制
            log.error(e.getMessage());

            ApiResult<?> result;

            if (e instanceof NotLoginException) {
                log.error("登录失效异常:{}", e.getMessage());
                result = ApiResult.error(ApiResultEnum.NOT_LOGIN.getCode(), ApiResultEnum.NOT_LOGIN.getDesc());
            } else if (e instanceof NotPermissionException) {
                log.error("权限不足异常:{}", e.getMessage());
                result = ApiResult.error(ApiResultEnum.NOT_PERMISSION.getCode(), ApiResultEnum.NOT_PERMISSION.getDesc());
            } else if (e instanceof BusinessException) {
                String errorMsg = ((BusinessException) e).getErrorMsg();
                log.error("业务逻辑异常: {}", errorMsg);
                errorMsg = StrUtil.isBlank(errorMsg) ? ApiResultEnum.ERROR.getDesc() : errorMsg;
                result = ApiResult.error(ApiResultEnum.ERROR.getCode(), errorMsg);
            } else if (e instanceof NotLogException) {
                result = ApiResult.error(ApiResultEnum.ERROR.getCode(), ApiResultEnum.ERROR.getDesc());
            } else if (e instanceof NotRoleException) {
                log.error("角色未知异常: {}", e.getMessage());
                result = ApiResult.error(ApiResultEnum.NOT_PERMISSION.getCode(), ApiResultEnum.NOT_PERMISSION.getDesc());
            } else if (e instanceof StatusException statusException){
                log.error("StatusException: {}", e.getMessage());
                result = ApiResult.error(statusException.getCode(), statusException.getMessage());
            } else {
                log.error("未知异常: {}", e.getMessage(), e);
                result = ApiResult.error(ApiResultEnum.ERROR.getDesc());
            }

            saveSystemLog(ctx, LogStatusEnum.ERROR.getCode());
            ctx.render(result);
        }
        //5.获得接口响应时长
        long times = System.currentTimeMillis() - start;
        log.info("用时：{}ms", times);
        saveSystemLog(ctx, LogStatusEnum.SUCCESS.getCode());
    }

    /**
     * 保存用户请求日志
     */
    private void saveSystemLog(Context ctx, int logStatus) {
        log.info("saveLog >>>>> 请求IP：{},请求地址：{},请求方式：{}", ctx.realIp(), ctx.path(), ctx.method());

        if (!appSystemProp.getSaveLog()) {
            log.warn("saveSystemLog >>>>> config: myapp.save-log=false");
            return;
        }

        Handler mainHandler = ctx.mainHandler();
        if (!(mainHandler instanceof Action action)) {
            log.warn("saveSystemLog >>>>> is not mainHandler");
            return;
        }
        LogPersistence logPersistence = action.method().getAnnotation(LogPersistence.class);
        if (logPersistence == null) {
            log.debug("saveSystemLog >>>>> LogPersistence is null");
            return;
        }
        if (logPersistence.logType() != LogTypeEnum.OPERATION.getCode()) {
            log.warn("saveSystemLog >>>>> is not LogTypeEnum.OPERATION");
            return;
        }
        BigInteger loginId = LoginUtils.loginId();
        if (loginId == null) {
            log.warn("saveSystemLog >>>>> is not login");
            return;
        }
        SysLogAddDTO sysLogAddDTO = new SysLogAddDTO()
                .setIpAddress(ctx.realIp())
                .setRequestWay(ctx.method())
                .setApiPath(ctx.path())
                .setLogType(logPersistence.logType())
                .setOperator(loginId)
                .setLogStatus(logStatus);
        ThreadUtil.execAsync(() -> sysLogService.add(sysLogAddDTO));
    }

    /**
     * 在线用户续期
     */
    private void onlineUsersRenewal() {
        if (StpUtil.isLogin()) {
            String loginId = String.valueOf(StpUtil.getLoginId());
            ThreadUtil.execAsync(() -> {
                String key = String.format(RedisKeyConst.ONLINE_USER, loginId);
                redisService.save(key, loginId, 30L);
            });
        }
    }

}
