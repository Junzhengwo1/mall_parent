package com.kou.gulimall.order.controller.web;

import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.order.entity.OrderEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 创建订单使用消息队列
 */
@Api(tags = "测试 | 测试下订单操作")
@RestController
public class RabbitOrderTestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ApiOperation("测试下单")
    @GetMapping("/test/creatOrder")
    public R createOrder(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        //给MQ发消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        return R.ok().setData(orderEntity);
    }

}
