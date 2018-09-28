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
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MessageHandler {

  final Gson gson = new Gson();
  final GatewayLocator gatewayLocator;
  final NotificationMessageFactory notificationMsgFactory = new NotificationMessageFactory();
  final MQProducer producer;
  final String pushTopic;
  final String pushTag;


  public MessageHandler(MQProducer producer, GatewayLocator gatewayLocator,
      String pushTopic, String pushTag) {
    this.producer = producer;
    this.gatewayLocator = gatewayLocator;
    this.pushTopic = pushTopic;
    this.pushTag = pushTag;
  }

  void onMessageReceived(String msg) throws Exception {
    log.debug("接收到的消息是：{}", msg);
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
            log.debug("发送消息到gateway {}", msgList);
            producer.send(msgList);
          } else {
            Optional<String> pushMessage = notificationMsgFactory
                .getPushMessage(receiver, header, messageBody.getData());

            pushMessage.ifPresent(message -> {
              List<MqMessage> msgList = new ArrayList<>();
              MqMessage mqMessage = new MqMessage(pushTopic, pushTag, pushMessage.get());
              msgList.add(mqMessage);
              try {
                producer.send(msgList);
              } catch (Exception ex) {
                log.error("离线push信息{},  失败:{}", mqMessage.toString(), ex);
              }
              log.debug("离线push信息:{}", pushMessage.get());
            });

          }
        }
      }
    } catch (JsonSyntaxException e) {
      // 数据格式错误，记录错误，直接跳过
      log.error("消息内容为：" + msg, e);
    }
  }
}