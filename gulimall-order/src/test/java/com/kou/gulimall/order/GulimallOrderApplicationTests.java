package com.kou.gulimall.order;

import com.kou.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * 测试一下 rabbitMQ
 * 如何创建 exchange queue Binding 的关系？
 *
 *
 */

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    void sengMessage2(){
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        entity.setId(1L);
        entity.setCreateTime(new Date());
        entity.setName("haha");
        rabbitTemplate.convertAndSend("hello-exchange","hello.java",entity);
        log.info("mes{}",entity);
    }

    @Test
    void sengMessage(){
        rabbitTemplate.convertAndSend("hello-exchange","hello.java","Hello King");
        log.info("mes{}","hello king");
    }

    @Test
    void creatExchange() {
        DirectExchange directExchange = new DirectExchange("hello-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange{}",directExchange);

    }

    @Test
    void creatqQueue() {
        Queue queue = new Queue("hello-queue",true,false,false);
        String s = amqpAdmin.declareQueue(queue);
        log.info("{}",s);

    }

    @Test
    void creatqBinding() {
        Binding binding = new Binding("hello-queue",
                Binding.DestinationType.QUEUE,
                "hello-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("{}",binding);

    }


}
