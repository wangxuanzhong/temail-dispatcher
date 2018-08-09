package com.syswin.temail.cdtp.dispatcher.push;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
public class ConsumerTest {
    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("cdtp-dispatcher-consumer-test");
        consumer.setNamesrvAddr("172.28.43.18:9876");
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.subscribe("defaultChannel", "*");
        consumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                System.out.println("消息id" + msg.getMsgId()
                        + ",消息体：" + new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        System.out.println("消费者启动成功……");
    }
}
