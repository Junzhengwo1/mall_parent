package com.kou.gulimall.order.config;

import com.kou.gulimall.order.interceptor.LoginUserInterCeptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器的使用方式
 * todo 暂时注释掉
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterCeptor interCeptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interCeptor)
        .addPathPatterns("/**");
    }
}
