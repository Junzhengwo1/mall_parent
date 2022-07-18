package com.kou.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }


    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue",true,false,false);
    }


    @Bean
    public Queue stockDelayQueue(){
        //设置属性
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }


    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }


    @Bean
    public Binding stockLockedBinding(){
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }


//    @PostConstruct
//    public void initRabbitTemplate(){
//
//        /**
//         * todo 服务器接受到消息回调
//         */
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            /**
//             *
//             * @param correlationData 当前消息的唯一关联数据 相当于就是消息的唯一ID
//             * @param b 消息是否成功收到
//             * @param s 失败的原因
//             */
//            @Override
//            public void confirm(CorrelationData correlationData, boolean b, String s) {
//                System.out.println("correlationData--["+correlationData+"]---->ack["+b+"]--->case["+s+"]");
//            }
//        });
//
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            /**
//             * todo 失败时 回调
//             * 消息抵达队列确认回调
//             * 只要消息没有投递给指定的队列 就会触发回调
//             * @param message 投递失败的消息的详情
//             * @param i 回复的状态码
//             * @param s 回复的文本内容
//             * @param s1 那个交换机
//             * @param s2 那个路由键
//             */
//            @Override
//            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
//                System.out.println("message->["+message+"]");
//            }
//        });
//
//
//
//    }
}
