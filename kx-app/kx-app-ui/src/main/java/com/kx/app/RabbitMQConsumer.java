package com.kx.app;

import com.kx.common.enums.MessageEnum;
import com.kx.common.exceptions.GraceException;
import com.kx.common.result.ResponseStatusEnum;
import com.kx.common.utils.JsonUtils;
import com.kx.service.base.RabbitMQConfig;
import com.kx.service.data.mo.MessageMO;
import com.kx.service.service.MsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//RabbitMQ消费者
@Slf4j
@Component
public class RabbitMQConsumer {

    @Autowired
    private MsgService msgService;

    /**
     * 监听队列
     * queues =监听的队列
     * @param payload 消息的载体
     * @param message
     */
    @RabbitListener(queues = {RabbitMQConfig.QUEUE_SYS_MSG})
    public void watchQueue(String payload, Message message) {
        log.info("消费者拿到一条消息:"+payload);
        //1.将消息队列的json消息转换成对应结构的POJO
        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);
        //2.取出消息的路由
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info(routingKey);

        // TODO: 下面这段代码可以优化，一个地方是参数优化，另外是枚举的判断优化
        //3.根据路由进行不同的业务操作:存不同的消息到mongodb
        if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.FOLLOW_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.FOLLOW_YOU.type,
                    null);
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.FOLLOW_YOU.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.COMMENT_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.COMMENT_VLOG.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.REPLY_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.REPLY_YOU.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_COMMENT.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_COMMENT.type,
                    messageMO.getMsgContent());
        } else {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

    }


}
