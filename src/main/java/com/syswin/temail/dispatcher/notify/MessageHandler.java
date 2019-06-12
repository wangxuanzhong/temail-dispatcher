package com.syswin.temail.dispatcher.notify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MessageHandler {

  private final Gson gson = new Gson();
  private final String pushTag;
  private final String pushTopic;
  private final MQMsgSender producer;
  private final PacketTypeJudge judger;
  private final Consumer<CDTPHeader> taskExecutor;
  private final ChannelStsLocator gatewayLocator;
  private final NotificationMessageFactory notificationMsgFactory;


  MessageHandler(MQMsgSender producer, ChannelStsLocator gatewayLocator, String pushTopic,
      String pushTag, PacketTypeJudge judger, Consumer<CDTPHeader> taskExecutor, NotificationMessageFactory
      notificationMsgFactory) {
    this.producer = producer;
    this.gatewayLocator = gatewayLocator;
    this.pushTopic = pushTopic;
    this.pushTag = pushTag;
    this.judger = judger;
    this.taskExecutor = taskExecutor;
    this.notificationMsgFactory = notificationMsgFactory;
  }

  void onMessageReceived(String msg) throws Exception {
    log.info("Dispatcher receive a message from MQ ：{}", msg);
    try {
      MessageBody messageBody = gson.fromJson(msg, MessageBody.class);
      if (messageBody != null) {
        CDTPHeader header = gson.fromJson(messageBody.getHeader(), CDTPHeader.class);
        if (header != null) {
          this.taskExecutor.accept(header);
          String receiver = messageBody.getReceiver();
          List<TemailAccountLocation> statusList = gatewayLocator.locate(receiver);
          List<TemailAccountLocation> mobileList = new ArrayList<>();
          log.info("all devices is {}", statusList);
          statusList.forEach(status -> {
            String platform = status.getPlatform();
            if (StringUtils.equalsIgnoreCase(platform, "ios")
                || StringUtils.equalsIgnoreCase(platform, "android")) {
              mobileList.add(status);
            }
          });
          boolean needLog = true;
          if (!statusList.isEmpty()) {//只要有设备在线，就走在线
            needLog = false;
            sendOnLineMessage(messageBody, header, receiver, statusList);
          }
          if (mobileList.isEmpty()) { //如果没有手机，那么可能需要走离线push
            if (judger.isToBePushedMsg(messageBody.getEventType())
                && !judger.isSenderEqualsToRecevier(header)) {
              needLog = false;
              sendOfflineMessage(messageBody, header, receiver);
            }
          }
          if (needLog) {
            log.warn(
                "No registered channel status was found, and the MQmsg is not private or although it is private but sender is same to receiver, skip pushing the msg : {}",
                msg);
          }
        }
      }
    } catch (
        JsonSyntaxException e) {
      // 数据格式错误，记录错误，直接跳过
      log.error("Invalid message format：{}", msg, e);
    }

  }

  private void sendOfflineMessage(MessageBody messageBody, CDTPHeader header, String receiver) {
    Optional<String> pushMessage = notificationMsgFactory
        .getPushMessage(receiver, header, messageBody.getData());
    pushMessage.ifPresent(message -> {
      List<MqMessage> msgList = new ArrayList<>();
      MqMessage mqMessage = new MqMessage(pushTopic, pushTag, pushMessage.get());
      msgList.add(mqMessage);
      try {
        producer.send(msgList);
      } catch (Exception ex) {
        log.error("Failed to push offLine message : {}", mqMessage, ex);
      }
      log.info("succeed to push offLine message : {}", pushMessage.get());
    });
  }

  private void sendOnLineMessage(MessageBody messageBody, CDTPHeader header, String receiver,
      List<TemailAccountLocation> statusList) {
    String payload = notificationMsgFactory.notificationOf(receiver, header, messageBody.getData());
    List<MqMessage> msgList = new ArrayList<>();
    Set<String> mqTags = new HashSet<>();
    statusList.forEach(status -> {
          String mqTag = status.getMqTag();
          if (!mqTags.contains(mqTag)) {
            mqTags.add(mqTag);
            msgList.add(new MqMessage(status.getMqTopic(), mqTag, payload));
          }
          log.info("Send message by MQ to gateway server {}", msgList);
        }
    );
    try {
      producer.send(msgList);
    } catch (Exception ex) {
      log.error("Fail to send message : {}", msgList, ex);
    }
  }

}