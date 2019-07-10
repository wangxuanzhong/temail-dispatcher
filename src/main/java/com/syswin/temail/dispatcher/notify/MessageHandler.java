/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import java.util.concurrent.atomic.AtomicReference;
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


  public MessageHandler(MQMsgSender producer, ChannelStsLocator gatewayLocator, String pushTopic,
      String pushTag, PacketTypeJudge judger, Consumer<CDTPHeader> taskExecutor,
      NotificationMessageFactory
          notificationMsgFactory) {
    this.producer = producer;
    this.gatewayLocator = gatewayLocator;
    this.pushTopic = pushTopic;
    this.pushTag = pushTag;
    this.judger = judger;
    this.taskExecutor = taskExecutor;
    this.notificationMsgFactory = notificationMsgFactory;
  }

  public  void onMessageReceived(String msg) throws Exception {
    log.info("Dispatcher receive a message from MQ ：{}", msg);
    try {
      MessageBody messageBody = gson.fromJson(msg, MessageBody.class);
      if (messageBody != null) {
        CDTPHeader header = gson.fromJson(messageBody.getHeader(), CDTPHeader.class);
        if (header != null) {
          this.taskExecutor.accept(header);
          String receiver = messageBody.getReceiver();
          List<TemailAccountLocation> statusList = gatewayLocator.locate(receiver);
          final AtomicReference<Boolean> containsMobile = new AtomicReference<>(false);
          statusList.forEach(status -> {
            if (StringUtils.equalsIgnoreCase(status.getPlatform(), "ios")
                || StringUtils.equalsIgnoreCase(status.getPlatform(), "android")) {
              containsMobile.set(true);
            }
          });
          sendOnLineMessage(messageBody, header, receiver, statusList);
          boolean needOfflnePush = judger.isToBePushedMsg(messageBody.getEventType())
              && !judger.isSenderEqualsToRecevier(header);
          if (!containsMobile.get() && needOfflnePush) {
            sendOfflineMessage(messageBody, header, receiver);
            return;
          }
          if (statusList.isEmpty() && !(needOfflnePush)) {
            log.warn(
                "No registered channel status was found, and the MQmsg is not private or although "
                    + "it is private but sender is same to receiver, skip pushing the msg : {}",
                msg);
          }
        }
      }
    } catch (
        JsonSyntaxException e) {
      log.error("Invalid message format：{}", msg, e);
    }

  }

  public void sendOfflineMessage(MessageBody messageBody, CDTPHeader header, String receiver) {
    Optional<String> pushMessage = notificationMsgFactory
        .getPushMessage(receiver, header, messageBody.getData());
    pushMessage.ifPresent(message -> {
      List<MqMessage> msgList = new ArrayList<>();
      MqMessage mqMessage = new MqMessage(pushTopic, pushTag, pushMessage.get());
      msgList.add(mqMessage);
      try {
        producer.send(msgList);
        log.info("succeed to push offLine message : {}", pushMessage.get());
      } catch (Exception ex) {
        log.error("Failed to push offLine message : {}", mqMessage, ex);
      }
    });
  }

  public void sendOnLineMessage(MessageBody messageBody, CDTPHeader header,
      String receiver, List<TemailAccountLocation> statusList) {
    if (statusList.isEmpty()) {
      return;
    }
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