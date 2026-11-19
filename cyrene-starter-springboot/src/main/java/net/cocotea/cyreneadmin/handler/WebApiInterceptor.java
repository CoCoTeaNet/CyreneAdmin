package net.cocotea.cyreneadmin.handler;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.cocotea.cyreneadmin.model.dto.SysLogAddDTO;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysLogService;
import net.cocotea.cyreneadmin.annotation.LogPersistence;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.LogStatusEnum;
import net.cocotea.cyreneadmin.enums.LogTypeEnum;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneadmin.util.IpUtils;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.math.BigInteger;

@Component
public class WebApiInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebApiInterceptor.class);

    /**
     * 异常处理器已写入错误日志的标记，请求属性名
     */
    public static final String REQUEST_LOG_SAVED_ATTR = "cyrene.request.log.saved";

    @Resource
    private RedisService redisService;

    @Resource
    private AppSystemProp appSystemProp;

    @Resource
    private SysLogService sysLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURL = request.getRequestURL().toString();
        // 是否静态路径
        boolean staticApi = isStaticApi(requestURL);
        // 打印请求内容
        logger.info(
                "preHandle >>> 请求IP：{}，请求地址：{}，请求方式，{}，请求方法：{}，是否静态地址：{}",
                request.getRemoteAddr(),
                requestURL,
                request.getMethod(),
                request.getRequestURI(),
                staticApi
        );
        if (staticApi) {
            return true;
        }
        // 在线用户续期
        if (StpUtil.isLogin()) {
            String loginId = String.valueOf(StpUtil.getLoginId());
            ThreadUtil.execAsync(() -> {
                String key = String.format(RedisKeyConst.ONLINE_USER, loginId);
                redisService.save(key, loginId, 30L);
            });
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 请求结束后再写成功日志：控制器抛异常时由全局异常处理器写错误日志，
     * 避免失败请求同时出现成功与失败两条日志。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null || request.getAttribute(REQUEST_LOG_SAVED_ATTR) != null) {
            return;
        }
        saveSystemLog(request, handler);
    }

    /**
     * 保存用户请求日志
     *
     * @param request {@link HttpServletRequest}
     * @param handler {@link HandlerMethod}
     */
    private void saveSystemLog(HttpServletRequest request, Object handler) throws BusinessException {
        if (!appSystemProp.getSaveLog()) {
            logger.warn("saveSystemLog >>>>> saveLog is not enable, sra-admin.save-log: false");
            return;
        }
        if (!(handler instanceof HandlerMethod method)) {
            logger.warn("saveSystemLog >>>>> is not HandlerMethod");
            return;
        }
        LogPersistence logPersistence = method.getMethodAnnotation(LogPersistence.class);
        if (logPersistence == null) {
            logger.debug("saveSystemLog >>>>> LogPersistence is null");
            return;
        }
        if (logPersistence.logType() != LogTypeEnum.OPERATION.getCode()) {
            logger.warn("saveSystemLog >>>>> is not LogTypeEnum.OPERATION");
            return;
        }
        BigInteger loginId = LoginUtils.loginId();
        if (loginId == null) {
            logger.warn("saveSystemLog >>>>> is not login");
            return;
        }
        SysLogAddDTO sysLogAddDTO = new SysLogAddDTO()
                .setIpAddress(IpUtils.getIp(request))
                .setRequestWay(request.getMethod())
                .setApiPath(request.getRequestURI())
                .setLogType(logPersistence.logType())
                .setOperator(loginId)
                .setLogStatus(LogStatusEnum.SUCCESS.getCode());
        ThreadUtil.execAsync(() -> sysLogService.add(sysLogAddDTO));
    }

    /**
     * 是否静态地址，比如/doc.html
     *
     * @param url 地址
     * @return 是返回TRUE
     */
    private boolean isStaticApi(String url) {
        String regex = "^https?://[^/]+(:\\d+)?(/[^?#]*\\.)(html|css|js|jpg|jpeg|gif|png|svg|woff|woff2|ttf|eot)$";
        return ReUtil.isMatch(regex, url);
    }
}
