package com.kou.gulimall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.kou.gulimall.cart.feign")
@EnableDiscoveryClient
//@MapperScan("com.kou.gulimall.cart.dao")
//todo  基于redis来实现的
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableSwagger2
public class GulimallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCartApplication.class, args);
    }

}
