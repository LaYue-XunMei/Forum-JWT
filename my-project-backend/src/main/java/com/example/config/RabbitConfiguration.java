package com.example.config;

import com.example.utils.Const;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置
 */
@Configuration
public class RabbitConfiguration {
    @Bean("mailQueue")
    public Queue queue(){
        return QueueBuilder
                .durable(Const.MQ_MAIL)
                .deadLetterExchange("dlx.direct")
                .deadLetterRoutingKey("error-message")
                .ttl(3 * 60 * 1000)
                .build();
    }

    //死信队列
    @Bean("errorQueue")
    public Queue dlQueue(){
        return QueueBuilder
                .durable(Const.MQ_ERROR)
                .ttl(24*60*60*1000)
                .build();
    }

    //死信队列交换机
    @Bean("errorExchange")
    public Exchange dlExchange(){
        return ExchangeBuilder
                .directExchange("dlx.direct")
                .build();
    }

    //绑定
    @Bean
    public Binding dlBinding(@Qualifier("errorExchange") Exchange exchange,
                             @Qualifier("errorQueue") Queue queue){
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("error-message")
                .noargs();
    }


    // 配置 JSON 消息转换器
    //把对象转换成JSON格式
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
