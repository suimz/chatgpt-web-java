package com.suimz.open.chatgptweb.java.core.conf;

import com.suimz.open.chatgptweb.java.core.interceptor.AuthInterceptor;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC Configuration
 *
 * @author https://github.com/suimz
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private AppProperties appProperties;
    @Resource
    private ResourceLoader resourceLoader;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:/static/index.html");
        if (resource.exists()) {
            registry.addViewController("/").setViewName("forward:/index.html");
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Client Authentication
        registry.addInterceptor(new AuthInterceptor(appProperties)).addPathPatterns("/**")
        .excludePathPatterns("/", "/api/verify", "/api/session", ".+\\.(html|css|js|gif|jpg|jpeg|png|ico|svg|ttf|woff|woff2)$");
    }

}
