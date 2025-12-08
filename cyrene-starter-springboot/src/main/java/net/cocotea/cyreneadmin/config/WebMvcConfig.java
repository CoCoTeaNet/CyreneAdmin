package net.cocotea.cyreneadmin.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import jakarta.annotation.Resource;
import net.cocotea.cyreneadmin.handler.WebApiInterceptor;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Resource
    private AppSystemProp appSystemProp;

    @Resource
    private WebApiInterceptor webApiInterceptor;

    @Bean
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = getFastJsonConfig();
        // 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return fastConverter;
    }

    private static FastJsonConfig getFastJsonConfig() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat(DatePattern.NORM_DATETIME_PATTERN);
        fastJsonConfig.setWriterFeatures(
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullNumberAsZero,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteBigDecimalAsPlain,
                JSONWriter.Feature.BrowserCompatible
        );
        return fastJsonConfig;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(fastJsonHttpMessageConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册注解拦截器，并排除不需要注解鉴权的接口地址 (与登录拦截器无关)
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(getExcludeList());
        // 自定义拦截器
        registry.addInterceptor(webApiInterceptor);
    }

    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter().setBeforeAuth(r -> SaHolder.getResponse()
                // 服务器名称
                .setServer("sa-server")
                // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                .setHeader("X-Frame-Options", "SAMEORIGIN")
                // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                .setHeader("X-XSS-Protection", "1; mode=block")
                // 禁用浏览器内容嗅探
                .setHeader("X-Content-Type-Options", "nosniff"));
    }

    /**
     * 获取放行路由列表
     *
     * @return 路由集合
     */
    public List<String> getExcludeList() {
        List<String> excludes = new ArrayList<>();
        Collections.addAll(excludes, appSystemProp.getExcludes().split(","));
        if (excludes.isEmpty()) {
            return Collections.emptyList();
        }
        logger.info("~~~~~~~~~~~ 放行路由 ~~~~~~~~~~~");
        excludes.forEach(logger::info);
        logger.info("~~~~~~~~~~~");
        return excludes;
    }
}
