package com.kou.gulimall.ware.listener;

import com.kou.gulimall.common.to.mq.OrderTo;
import com.kou.gulimall.common.to.mq.StockLockedTo;
import com.kou.gulimall.ware.service.WareSkuService;
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
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {


    @Autowired
    private WareSkuService wareSkuService;



    /**
     * todo 自动解库存 监听消息对列
     *  【情况】1、下单成功，库存也成功但是接下来的业务失败，导致订单回滚，那么库存也该解锁，之前用的seata实现就太慢了
     *  【情况】2、本身就是库存锁失败
     */
    @RabbitHandler
    public void handleStockLockedRelese(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("收到解锁库存信息{}",to);
        try {
            wareSkuService.releaseLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            log.error("错误……{}",e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to,Message message, Channel channel) throws IOException {
        log.info("收到准备解锁的订单数据{}",to);
        try {
            wareSkuService.releaseLockStock(to);//重载
        } catch (Exception e) {
            log.info("错误……{}",e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }


    }



}
