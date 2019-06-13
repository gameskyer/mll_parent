package com.xmcc.mll_order.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration//表示为配置类
public class rabbitmqConfig {
    @Bean
    //消息转换器 使用同一类转换器 方便对象传输
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
