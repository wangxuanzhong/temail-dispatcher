package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
public class RocketDispatchListener implements MessageListenerConcurrently {

  private final MessageHandler messageHandler;

  public RocketDispatchListener(MQMsgSender producer, ChannelStsLocator gatewayLocator, DispatcherProperties properties,
      PacketTypeJudge packetTypeJudge) {
    messageHandler = new MessageHandler(producer, gatewayLocator, properties.getRocketmq().getPushTopic(),
        properties.getRocketmq().getPushTag(), packetTypeJudge);
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
