package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.common.message.Message;

/**
 * @author 姚华成
 * @date 2018-9-5
 */
class RocketMQProducer implements MQProducer {

  private org.apache.rocketmq.client.producer.MQProducer producer;

  public RocketMQProducer(org.apache.rocketmq.client.producer.MQProducer producer) {
    this.producer = producer;
  }

  @Override
  public void send(List<MqMessage> mqMessages) throws Exception {
    List<Message> messageList = new ArrayList<>(mqMessages.size());
    for (MqMessage msg : mqMessages) {
      messageList.add(new Message(msg.getGatewayFlag(), msg.getInstanceFlag(), msg.getBody().getBytes()));
    }
    producer.send(messageList);
  }
}
