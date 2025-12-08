package net.cocotea.cyreneadmin.config;

import net.cocotea.cyreneadmin.service.*;
import net.cocotea.cyreneadmin.service.impl.*;
import net.cocotea.cyreneadmin.util.SecurityUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class ServiceBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        final String lightDaoRef = "lightDao";
        final String appSystemPropRef = "appSystemProp";
        final String redisClientRef = "redisClient";
        final String redisServiceRef = "redisService";
        final String securityUtilsRef = "securityUtils";

        registerBeanDefinition(registry, "redisService", RedisServiceImpl.class, redisClientRef);
        registerBeanDefinition(registry, "securityUtils", SecurityUtils.class, appSystemPropRef);

        registerBeanDefinition(registry, "sysDictionaryService", SysDictionaryServiceImpl.class, lightDaoRef);
        registerBeanDefinition(registry, "sysLogService", SysLogServiceImpl.class, appSystemPropRef, lightDaoRef);
        registerBeanDefinition(registry, "sysDashboardService", SysDashboardServiceImpl.class, lightDaoRef, redisServiceRef);
        registerBeanDefinition(registry, "sysRoleService", SysRoleServiceImpl.class, lightDaoRef);
        registerBeanDefinition(registry, "sysMenuService", SysMenuServiceImpl.class, lightDaoRef, redisServiceRef);
        registerBeanDefinition(registry, "sysUserService", SysUserServiceImpl.class, appSystemPropRef, redisServiceRef, securityUtilsRef, lightDaoRef);
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass, String... beanNames) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        for (String beanNameRef : beanNames) {
            beanDefinitionBuilder.addConstructorArgReference(beanNameRef);
        }
        registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

}
