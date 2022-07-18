package com.kou.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;




    @Bean
    public MessageConverter  messageConverter(){
        return new Jackson2JsonMessageConverter();
    }




    /**
     * 容器中的这些组件 只需要用@Bean 就会在MQ中自动创建
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        //设置属性
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }


    /**
     * 订单释放直接与库存释放绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }



    @PostConstruct
    public void initRabbitTemplate(){

        /**
         * todo 服务器接受到消息回调
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据 相当于就是消息的唯一ID
             * @param b 消息是否成功收到
             * @param s 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                /**
                 * todo 1、做好消息确认消息机制
                 *      2、每一个发送的消息都在数据库做好记录，定期将消息再次发送
                 */
                System.out.println("correlationData--["+correlationData+"]---->ack["+b+"]--->case["+s+"]");
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * todo 失败时 回调
             * 消息抵达队列确认回调
             * 只要消息没有投递给指定的队列 就会触发回调
             * @param message 投递失败的消息的详情
             * @param i 回复的状态码
             * @param s 回复的文本内容
             * @param s1 那个交换机
             * @param s2 那个路由键
             */
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                //能来到这个方法那说明已经时是出问题了
                //处理问题

                System.out.println("message->["+message+"]");
            }
        });



    }
}
