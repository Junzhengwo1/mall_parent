package com.kou.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 引入了qmqp 后就自动生效了
 * 并且注入了对应的的bean
 *
 * todo 监听信息注解： RabbitListener 可以标的类上
 *                    RabbitHandler 可以标在方法上 来接受不同类型的消息
 *
 * todo 消息队列的消息，可以是多个接受者，但是只能有一个能收到，收到后，就没消息了
 * todo 只有一个消息完全处理完之后，才会去接受下一个消息
 *
 * RabbitMQ的确认机制-可靠抵达
 *
 *  todo 消费端 确认消息时
 *      只要时手动模式
 *      ：只要我们没手动处理；消息也不会丢失
 *
 *
 *订单服务 有拦截器，只有登录状态的情况下，才可以访问
 *
 * todo  本地事务，失效问题：
 *      就是同一个service中不同方法之间相互调用 事务是失效的；
 *      原因就是绕过了代理对象来做事情
 *      解决方法：
 *          1）.引入aop-starter aspectj
 *          2).开启 @EnableAspectJAutoProxy(exposeProxy = true)
 *          3)用代理对象来本类互调
 *
 *
 * TODO 【分布式事务】
 *  1：CAP: p是一定要满足的
 *  2: Raft: 算法网址 thesecretlivesofdata.com
 *
 * TODO 【seata】Alibaba 提供的分布式事务方案
 *      1.给每张表建undo_log 表
 *      2.安装seata 的服务器【并配置相关】【
 *          registry.config 配置注册中心，他自己本身也相当一个微服务需要启动起来
 *      3.所有想要用到分布式事务的微服务使用 DataSourceProxy代理自己的数据源
 *      4.每个微服务都必须导入seata 的通信配置
 *
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableCaching
@EnableTransactionManagement
@EnableFeignClients(basePackages = "com.kou.gulimall.order.feign")
@EnableDiscoveryClient
@MapperScan("com.kou.gulimall.order.dao")
@SpringBootApplication
@EnableSwagger2
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
