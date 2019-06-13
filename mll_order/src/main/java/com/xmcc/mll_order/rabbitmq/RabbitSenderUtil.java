package com.xmcc.mll_order.rabbitmq;

import com.xmcc.mll_order.mapper.MllMessageMapper;
import com.xmcc.mllcommon.common.Constant;
import com.xmcc.mllcommon.common.MessageStatus;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.rabbit.MllMessage;
import com.xmcc.mllcommon.util.DateUtil;
import com.xmcc.mllcommon.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Component
@Slf4j
public class RabbitSenderUtil {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageConverter messageConverter;

    @Autowired
    private MllMessageMapper mllMessageMapper;

    //定义confirm 确认监听回调函数  是否成功将消息发送到rabbitmq的broker 都会执行该方法
    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        //先看第二个参数 就是消息是否发送到了broker的结果
        //第一个参数为传递的消息的信息 包含了全局唯一id
        @Override
        public void confirm(CorrelationData correlationData, boolean b, String s) {
            log.info("confirm消息的id为：{}",correlationData.getId());
            log.info("confirm消息的结果为：{}",b );
            if (!b){
                mllMessageMapper.updateStatusById(MessageStatus.SEND_ERROR.getCode(), Long.valueOf(correlationData.getId()));
            }
        }
    };

    //定义return的监听回调函数  不能将消息路由到指定queue 就会指定该回调方法
    final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
        //参数：发送的消息  错误码  错误文本信息  交换机  路由键
        @Override
        public void returnedMessage(Message message, int relayCode, String relayText,
                                    String exchange, String routingKey) {
            log.info("return消息为：{}",relayText);
            Long id = Long.valueOf(message.getMessageProperties().getMessageId());
            mllMessageMapper.updateStatusById(MessageStatus.SEND_ERROR.getCode(),id );
        }
    };
    //定义过期时间
    public static final long EXPIRE=60*1000;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void OrderErrorToProduct(Long id,MllOrderDTO mllOrderDTO){
        //将该条消息的全局ID设置进入CorrelationData方便confirm中取出来 唯一标识该消息
        CorrelationData correlationData = new CorrelationData(String.valueOf(id));
        MessageProperties messageProperties = new MessageProperties();
        //设置messageId 在消费端取出来可以判断 重复消费
        messageProperties.setMessageId(String.valueOf(id));
        Message message = messageConverter.toMessage(mllOrderDTO,messageProperties );
        //加入消息确认模式的监听
        rabbitTemplate.setConfirmCallback(confirmCallback);
        //加入消息return模式的监听
        rabbitTemplate.setReturnCallback(returnCallback);

        //将消息插入数据库
        try {
            log.info("将消息插入到数据库" );
            //标记消息状态为成功
            int insert = mllMessageMapper.insert(MllMessage.builder().messageId(id).messageBody(JsonUtil.object2string(mllOrderDTO)).
                    expire(DateUtil.expire(EXPIRE)).status(MessageStatus.SEND_SUCCESS.getCode()).build());
            log.info("插入数据库的结果为：{}",insert );
        }catch (DuplicateKeyException e){
            log.info("插入异常，进行修改" );
            //增加次数与修改过期时间
            mllMessageMapper.updateSendCount(DateUtil.expire(EXPIRE), MessageStatus.SEND_SUCCESS.getCode(), id);
        }
        //1.交换机名称  2.routingKey 3.发送的消息数据 4.该消息的信息 例如全局唯一的id设置6
        rabbitTemplate.convertAndSend(Constant.OrderProduct.ORDER_TO_PRODUCT_EXCHANGE,
                Constant.OrderProduct.ORDER_TO_PRODUCT_ROLLBACK, message,correlationData);
    }

}
