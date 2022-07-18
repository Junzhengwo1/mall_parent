package com.kou.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.order.dao.OrderItemDao;
import com.kou.gulimall.order.entity.OrderItemEntity;
import com.kou.gulimall.order.entity.OrderReturnReasonEntity;
import com.kou.gulimall.order.service.OrderItemService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@RabbitListener(queues = {"hello-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * todo 测试监听消息
     *  手动处理接受消息 确认 防止消息丢失
     */
    @RabbitHandler
    public void reciveMessage(Message message, OrderReturnReasonEntity reasonEntity, Channel channel) {

        //在通道里面 确认消息是否交付
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //签收 非批量
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("接受到消息了"+message+"内容"+reasonEntity);
    }

}