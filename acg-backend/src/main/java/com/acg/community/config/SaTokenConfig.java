package com.acg.community.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // Sa-Token 会自动校验登录状态
        })).addPathPatterns("/**")
           .excludePathPatterns(
               "/user/login",
               "/user/register",
               "/product/list",
               "/product/{id}",
               "/makeup/services",
               "/makeup/service/{id}",
               "/makeup/slot/available",
               "/category/list",
               "/chat/messages"
           );
    }
}