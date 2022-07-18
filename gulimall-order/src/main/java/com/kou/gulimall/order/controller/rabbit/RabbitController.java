package com.kou.gulimall.order.controller.rabbit;

import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.order.entity.OrderReturnReasonEntity;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
@Api(tags = "Rabbit消息测试")
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMsg")
    public R sendMsg(){
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        entity.setId(1L);
        entity.setCreateTime(new Date());
        entity.setName("haha");
        rabbitTemplate.convertAndSend("hello-exchange","hello.java",entity,new CorrelationData(UUID.randomUUID().toString()));
        log.info("mes{}",entity);
        return R.ok();
    }

}
