package com.syswin.temail.dispatcher.notify;

import com.syswin.library.messaging.rocketmq.RocketMqProducer;
import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import java.util.Iterator;
import java.util.List;

public class CommonMQMsgSender implements MQMsgSender {

  private RocketMqProducer rocketMqProducer;

  public CommonMQMsgSender(RocketMqProducer rocketMqProducer) {
    this.rocketMqProducer = rocketMqProducer;
  }

  @Override
  public void send(List<MqMessage> mqMessages) throws Exception {
    for (Iterator<MqMessage> iterator = mqMessages.iterator(); iterator.hasNext(); ) {
      MqMessage message =  iterator.next();
      rocketMqProducer.send(message.getBody(),message.getGatewayFlag(),message.getInstanceFlag(),"");
    }
  }
}
