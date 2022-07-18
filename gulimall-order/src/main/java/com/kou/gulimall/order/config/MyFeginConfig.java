package com.kou.gulimall.order.config;

import com.alibaba.fastjson.JSON;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * fegin 拦截器
 */
@Slf4j
@Configuration
public class MyFeginConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //todo 拿到当前请求的上下文
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attributes != null){
                    //老请求
                    HttpServletRequest oldRequest = attributes.getRequest();
                    //新请求同步请求头数据： cookie
                    requestTemplate.header("Cookie",oldRequest.getHeader("Cookie"));
                    log.info("fegin 在调用 前使用拦截器{}", JSON.toJSONString(requestTemplate));
                }
            }
        };
    }
}
