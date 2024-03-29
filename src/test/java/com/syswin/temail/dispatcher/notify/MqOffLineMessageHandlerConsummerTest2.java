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

import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

@Ignore
public class MqOffLineMessageHandlerConsummerTest2 {

  private final static Gson gson = new Gson();
  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
  //private final MQMsgSender producer = Mockito.mock(MQMsgSender.class);
  private final RemoteChannelStsLocator gatewayLocator = Mockito
      .mock(RemoteChannelStsLocator.class);

  private final String recipient = "sean@t.email";

  private final MessageBody pushDatapayload = mqPushMsgPayload(recipient, "bonjour");

  private final DispatcherProperties properties = new DispatcherProperties();


  private byte[] currentMessage;


  static MessageBody mqPushMsgPayload(String recipient, String message) {
    MessageBody payload = new MessageBody();
    payload.setReceiver(recipient);

    CDTPHeader header = new CDTPHeader();
    header.setReceiver(recipient);
    payload.setHeader(gson.toJson(header));

    PushData pushData = new PushData();
    pushData.setMsgId("pushMsgId1");
    pushData.setFrom("jack@t.email");
    pushData.setTo(recipient);
    pushData.setMessage(message);
    pushData.setEventType(0);
    payload.setData(gson.toJson(pushData));
    return payload;
  }


  @Pact(provider = "temail-notificaiton-mq", consumer = "temail-dispatcher-mq")
  public MessagePact createPact(MessagePactBuilder builder) {
    PactDslJsonBody pushJsonBody = packetJson(pushDatapayload);

    Map<String, String> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json; charset=UTF-8");
    return builder.given("Notification service is available")
        .expectsToReceive("offline notification")
        .withMetadata(metadata)
        .withContent(pushJsonBody)
        .toPact();
  }


  @Test
  @PactVerification({"Able to process offline notification message"})
  public void testPush() throws Exception {
    // 1. 请求参数的准备：
    // 2. 外部环境的模拟：
    //    2.1 gatewayLocator获取gateway信息；
    //    2.2 producer发送消息内容
    //          发送的消息内容的构建
    // 3. 执行需要验证的方法
    // 4. 检查结果

//    DefaultMQProducer producer = new DefaultMQProducer(properties.getRocketmq().getProducerGroup());
//    producer.setNamesrvAddr(properties.getRocketmq().getNamesrvAddr());
//    producer.start();

    DefaultMQProducer producer = new DefaultMQProducer("temail-dispatcher-producer");
    producer.setNamesrvAddr("192.168.15.37:9876");
    producer.start();

    RocketMQProducer rocketMQProducer = new RocketMQProducer(producer);

    String mqTopic = "mqTopic";
    String mqTag = "mqTag";

    List<TemailAccountLocation> locations = new ArrayList<>();
    //locations.add(new TemailAccountLocation(recipient, "", "", "", mqTopic, mqTag));
    when(gatewayLocator.locate(recipient)).thenReturn(locations);

    NotificationMessageFactory notificationMsgFactory = new NotificationMessageFactory();
    Optional<String> body = notificationMsgFactory
        .getPushMessage(pushDatapayload.getReceiver(),
            gson.fromJson(pushDatapayload.getHeader(), CDTPHeader.class),
            pushDatapayload.getData());

    MessageHandler messageHandler = new MessageHandler(rocketMQProducer, gatewayLocator,
        "temail-message", "*", new PacketTypeJudge(null), t -> {
    }, new NotificationMessageFactory());

    for (int i = 0; i < 5; i++) {
      messageHandler.onMessageReceived(gson.toJson(pushDatapayload));
    }
  }

  public void setMessage(byte[] messageContents) {
    currentMessage = messageContents;
  }

  private ArgumentMatcher<List<MqMessage>> matchesPayload(List<MqMessage> messageList) {
    return params -> gson.toJson(messageList)
        .equals(gson.toJson(params));
  }

  private PactDslJsonBody packetJson(MessageBody messageBody) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringValue("receiver", messageBody.getReceiver());
    body.stringValue("header", messageBody.getHeader());
    body.stringValue("data", messageBody.getData());
    return body;
  }

}
