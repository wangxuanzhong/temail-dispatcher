package com.syswin.temail.dispatcher.notify;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans.Header;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

public class MqOffLineMessageHandlerConsummerTest {

  private final static Gson gson = new Gson();
  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
  private final MQProducer producer = Mockito.mock(MQProducer.class);
  private final GatewayLocator gatewayLocator = Mockito.mock(GatewayLocator.class);

  private final String recipient = "sean@t.email";

  private final MessageBody pushDatapayload = mqPushMsgPayload(recipient, "bonjour");

  private final DispatcherProperties properties = new DispatcherProperties();


  private byte[] currentMessage;


  static MessageBody mqPushMsgPayload(String recipient, String message) {
    MessageBody payload = new MessageBody();
    payload.setReceiver(recipient);

    Header header = new Header();
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

    String mqTopic = "mqTopic";
    String mqTag = "mqTag";

    List<TemailAccountLocation> locations = new ArrayList<>();
    //locations.add(new TemailAccountLocation(recipient, "", "", "", mqTopic, mqTag));
    when(gatewayLocator.locate(recipient)).thenReturn(locations);

    NotificationMessageFactory notificationMsgFactory = new NotificationMessageFactory();
    Optional<String> body = notificationMsgFactory
        .getPushMessage(pushDatapayload.getReceiver(), gson.fromJson(pushDatapayload.getHeader(), Header.class),
            pushDatapayload.getData());

    List<MqMessage> msgList = new ArrayList<>();
    msgList
        .add(new MqMessage(properties.getRocketmq().getPushTopic(), properties.getRocketmq().getPushTag(), body.get()));

    MessageHandler messageHandler = new MessageHandler(producer, gatewayLocator, properties.getRocketmq().getPushTopic(), properties.getRocketmq().getPushTag());
    messageHandler.onMessageReceived(new String(currentMessage));
    verify(producer).send(argThat(matchesPayload(msgList)));
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
