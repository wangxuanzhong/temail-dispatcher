package com.syswin.temail.dispatcher.notify;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
class RocketDispatchListener implements MessageListenerConcurrently {

  private final MessageHandler messageHandler;

  public RocketDispatchListener(MQProducer producer, GatewayLocator gatewayLocator) {
    messageHandler = new MessageHandler(producer, gatewayLocator);
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        messageHandler.onMessageReceived(new String(msg.getBody()));
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.error("队列传输出错！请求参数：" + msgs, e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

}