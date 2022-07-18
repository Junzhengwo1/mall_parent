package com.kou.gulimall.order.listener;


import com.kou.gulimall.order.entity.OrderEntity;
import com.kou.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
@RabbitListener(queues = "order.release.order.queue")//能到这个队列的消息，都是延时队列过来的消息；做该做的操作
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    /**
     * 监听队列
     */
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        log.info("收到 过期订单，准备关闭订单----------------->{}",orderEntity);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error("错误……{}",e.getMessage());
            channel.basicReject(deliveryTag,true);

        }
    }


}
