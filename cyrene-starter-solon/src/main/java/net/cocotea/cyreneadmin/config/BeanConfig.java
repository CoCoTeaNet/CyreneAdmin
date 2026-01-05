package net.cocotea.cyreneadmin.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.cocotea.cyreneadmin.filter.ParameterInterceptor;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.*;
import net.cocotea.cyreneadmin.service.impl.*;
import net.cocotea.cyreneadmin.util.SecurityUtils;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.noear.redisx.RedisClient;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.validation.annotation.Valid;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

@Configuration
public class BeanConfig {

    @Bean
    public AppSystemProp buildDefaultProp(@Inject("${myapp.excludes}") String excludes,
                                          @Inject("${myapp.strong-password}") String strongPassword,
                                          @Inject("${myapp.password}") String password,
                                          @Inject("${myapp.password-salt}") String passwordSalt,
                                          @Inject("${myapp.permission-cache}") Boolean permissionCache,
                                          @Inject("${myapp.save-log}") Boolean saveLog,
                                          @Inject("${myapp.file.avatar}") String avatarPath,
                                          @Inject("${myapp.file.support-filetype}") String supportFiletype) {
        return new AppSystemProp().setExcludes(excludes)
                .setStrongPassword(strongPassword)
                .setPassword(password)
                .setPasswordSalt(passwordSalt)
                .setPermissionCache(permissionCache)
                .setSaveLog(saveLog)
                .setAvatarPath(avatarPath)
                .setSupportFiletype(supportFiletype);
    }

    @Bean
    public void init(AppContext context,
                     @Db LightDao lightDao,
                     @Inject AppSystemProp systemProp,
                     @Inject RedisClient redisClient) {
        context.wrapAndPut(RedisService.class, new RedisServiceImpl(redisClient));
        RedisService redisService = context.getBean(RedisService.class);

        context.wrapAndPut(SecurityUtils.class, new SecurityUtils(systemProp));
        SecurityUtils securityUtils = context.getBean(SecurityUtils.class);

        context.wrapAndPut(SysDictionaryService.class, new SysDictionaryServiceImpl(lightDao));
        context.wrapAndPut(SysLogService.class, new SysLogServiceImpl(systemProp, lightDao));
        context.wrapAndPut(SysDashboardService.class, new SysDashboardServiceImpl(lightDao, redisService));
        context.wrapAndPut(SysRoleService.class, new SysRoleServiceImpl(lightDao));
        context.wrapAndPut(SysMenuService.class, new SysMenuServiceImpl(lightDao, redisService));
        context.wrapAndPut(SysUserService.class, new SysUserServiceImpl(systemProp, redisService, securityUtils, lightDao));

        context.beanInterceptorAdd(Valid.class, new ParameterInterceptor());
    }

    @Bean
    public Validator myValidator() {
        try (ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()) {
            return factory.getValidator();
        }
    }

}
