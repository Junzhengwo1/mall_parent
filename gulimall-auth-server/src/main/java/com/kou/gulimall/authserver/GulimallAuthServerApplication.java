package com.kou.gulimall.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * todo  redisHttpSession :原理： （装饰者模式） 对原生的进行了重写
 *      【说白了就是redis在session原有的基础上接管了session的各种操作】
 *      1、会给容器中添加一个组件
 *      相当于将session 直接递交给了 redis来操作。
 *          由redis来操作session
 *      2、sessionRepositoryFilter:
 *          存储过滤器：
 *          创建的时候，会从容器中获取redisSessionRepository
 *          1、创建的时候，就会自动从容器中获取到sessionRepository；
 *          2、原始的request，respone都被包装
 *          3、以后获取Session.request.getSession()
 *          4、wappedRequest.getSession();  ==>SessionRepository 中获取到。
 *

 *
 */

@EnableRedisHttpSession  //整合redis进行session存储
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@EnableSwagger2
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
