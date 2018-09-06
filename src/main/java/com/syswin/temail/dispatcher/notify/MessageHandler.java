package com.syswin.temail.dispatcher.notify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans.Header;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MessageHandler {

  final Gson gson = new Gson();
  final GatewayLocator gatewayLocator;
  final NotificationMessageFactory notificationMsgFactory = new NotificationMessageFactory();
  final MQProducer producer;

  public MessageHandler(MQProducer producer, GatewayLocator gatewayLocator) {
    this.producer = producer;
    this.gatewayLocator = gatewayLocator;
  }

  void onMessageReceived(String msg) throws Exception {
    log.info("接收到的消息是：{}", msg);
    try {
      MessageBody messageBody = gson.fromJson(msg, MessageBody.class);
      if (messageBody != null) {
        Header header = gson.fromJson(messageBody.getHeader(), Header.class);
        if (header != null) {
          String receiver = messageBody.getReceiver();
          List<TemailAccountLocation> statusList = gatewayLocator.locate(receiver);

          if (!statusList.isEmpty()) {
            String payload = notificationMsgFactory.notificationOf(receiver, header, messageBody.getData());
            List<MqMessage> msgList = new ArrayList<>();
            Set<String> mqTags = new HashSet<>();
            statusList.forEach(status -> {
                  String mqTag = status.getMqTag();
                  if (!mqTags.contains(mqTag)) {
                    mqTags.add(mqTag);
                    msgList.add(new MqMessage(status.getMqTopic(), mqTag, payload));
                  }
                }
            );
            log.info("发送消息到gateway {}", msgList);
            producer.send(msgList);
          }
        }
      }
    } catch (JsonSyntaxException e) {
      log.error("消息内容为：{}", msg);
      log.error("解析错误", e);
      // 不处理，不重试
    }
  }
}