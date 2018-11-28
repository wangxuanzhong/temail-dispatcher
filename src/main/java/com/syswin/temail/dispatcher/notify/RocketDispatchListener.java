package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.DispatcherProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
class RocketDispatchListener implements MessageListenerConcurrently {

  private final MessageHandler messageHandler;

  public RocketDispatchListener(MQProducer producer, GatewayLocator gatewayLocator, DispatcherProperties properties) {
    messageHandler = new MessageHandler(producer, gatewayLocator, properties.getRocketmq().getPushTopic(),
        properties.getRocketmq().getPushTag());
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        messageHandler.onMessageReceived(new String(msg.getBody()));
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.error("fail to send message!  param: {}", msgs, e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

}
