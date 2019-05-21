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
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;


@Ignore
public class MqMessageHandlerConsumerTest {

  private final static Gson gson = new Gson();
  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
  private final MQMsgSender producer = Mockito.mock(MQMsgSender.class);
  private final RemoteChannelStsLocator gatewayLocator = Mockito
      .mock(RemoteChannelStsLocator.class);

  private final String recipient = "sean@t.email";
  private final MessageBody payload = mqMsgPayload(recipient, "bonjour");

  private final DispatcherProperties properties = new DispatcherProperties();

  private byte[] currentMessage;

  static MessageBody mqMsgPayload(String recipient, String message) {
    MessageBody payload = new MessageBody();
    payload.setReceiver(recipient);

    CDTPHeader header = new CDTPHeader();
    header.setReceiver(recipient);
    payload.setHeader(gson.toJson(header));

    Response<String> body = Response.ok(message);
    payload.setData(gson.toJson(body));
    return payload;
  }

  @Pact(provider = "temail-notificaiton-mq", consumer = "temail-dispatcher-mq")
  public MessagePact createPact(MessagePactBuilder builder) {
    PactDslJsonBody jsonBody = packetJson(payload);

    Map<String, String> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json; charset=UTF-8");

    return builder.given("Notification service is available")
        .expectsToReceive("online notification")
        .withMetadata(metadata)
        .withContent(jsonBody)
        .toPact();
  }

  @Test
  @PactVerification({"Able to process online notification message"})
  public void test() throws Exception {
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
    locations.add(new TemailAccountLocation(recipient, "", "", "", mqTopic, mqTag));
    when(gatewayLocator.locate(recipient)).thenReturn(locations);

    List<MqMessage> msgList = new ArrayList<>();
    NotificationMessageFactory notificationMsgFactory = new NotificationMessageFactory();
    String body = notificationMsgFactory
        .notificationOf(payload.getReceiver(), gson.fromJson(payload.getHeader(), CDTPHeader.class),
            payload.getData());
    msgList.add(new MqMessage(mqTopic, mqTag, body));

    MessageHandler messageHandler = new MessageHandler(producer, gatewayLocator,
        properties.getRocketmq().getPushTopic(), properties.getRocketmq().getPushTag(),
        new PacketTypeJudge(null), t -> {
    }, new NotificationMessageFactory());
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