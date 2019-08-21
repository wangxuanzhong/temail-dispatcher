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
    locations.add(new TemailAccountLocation(recipient, "", "pc", "", "", mqTopic, mqTag));
    locations.add(new TemailAccountLocation(recipient, "", "android", "", "", mqTopic, mqTag));
    locations.add(new TemailAccountLocation(recipient, "", "ios", "", "", mqTopic, mqTag));
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

  @Test
  @Ignore
  public void messageHandler() throws Exception {
    String message = "{\"receiver\":\"a.form@mailtest1.com\",\"eventType\":0,\"header\":\"{\\\"deviceId\\\":\\\"f7f14d72-2da2-4c56-8819-d96ab9bbe9f9\\\",\\\"signatureAlgorithm\\\":2,\\\"signature\\\":\\\"MIGIAkIBe-oDg3cfcG1_OasC7DwAbY8cMCtahn5Yi-fcDTqin71ZOjzzP5JWAuyZ37ZFqSXWzBLw32kw_L4J4LQYtyJdnXgCQgHvmbIG8oVqBLJ2ThqhBPi0StH-tf0P--cNWhor7sAhiVYprt5uz-nTHeiMZ1mTe3Doq9dc3gwVqrEogXbdjHUZvA\\\",\\\"dataEncryptionMethod\\\":4,\\\"timestamp\\\":1566338400020,\\\"packetId\\\":\\\"e69e738f-df40-44cf-82d5-5adcfc543651\\\",\\\"sender\\\":\\\"f.aaa112@mailtest1.com\\\",\\\"senderPK\\\":\\\"MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBKTTeMU5RcD6WnMGgpEfgdeEVc3uaZctN2uXBRCo0hQ_GUN6mxWchEa7ocb9jOPb94QIRv2mhkng3Fc38ugMVSgQAkQXBgymAReRrZymZ3BfdGiJ8MQHSHtODWF8g8uMnKcalRF7GGcot37AAVNFRPT0WHBHFm3Ujx2D68XVESyTTRuU\\\",\\\"receiver\\\":\\\"a.form@mailtest1.com\\\",\\\"receiverPK\\\":\\\"MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAbQVviIxHDMW84fNeBi6hS3uI885bUNdPiRC9Z753PdrC-N-ccNHE1MsYuwQaQ4uQZw6NbndlJl3eC6yRJxCI7gQA6_kQy5TwspEQ9cgJx7ULSQJS26Oet-aiqU4kw5aKIa58wYqrbdLL9MCWqXDx7YxY9HaimwZUW9mXNpuzGZwKTKk\\\",\\\"extraData\\\":\\\"eyJmcm9tIjoiZi5hYWExMTJAbWFpbHRlc3QxLmNvbSIsIm1zZ0lkIjoiYzIyMjMyZGMtZjMwNC00YzAxLTlmNGEtOWYyM2ZkMzYwMDczIiwic2Vzc2lvbkV4dERhdGEiOiJ7XCJhdmF0YXJIb3N0XCI6XCJodHRwOi8vbXNnc2VhbC5zeXN0b29udGVzdC5jb20vdGVtYWlsaW1hZ2VzL1wiLFwiY29udGFjdFR5cGVcIjo1LFwibmFtZVwiOlwi5rWL6K-VMlwifSIsInN0b3JlVHlwZSI6MSwidG8iOiJhLmZvcm1AbWFpbHRlc3QxLmNvbSIsInR5cGUiOiIwIn0\\\\u003d\\\",\\\"targetAddress\\\":\\\"msgseal.systoontest.com:8099\\\"}\",\"data\":\"{\\\"author\\\":\\\"\\\",\\\"attachmentSize\\\":0,\\\"sessionExtData\\\":\\\"{\\\\\\\"avatarHost\\\\\\\":\\\\\\\"http://msgseal.systoontest.com/temailimages/users/images/\\\\\\\",\\\\\\\"contactType\\\\\\\":5,\\\\\\\"name\\\\\\\":\\\\\\\"测试2\\\\\\\"}\\\",\\\"owner\\\":\\\"a.form@mailtest1.com\\\",\\\"xPacketId\\\":\\\"e69e738f-df40-44cf-82d5-5adcfc543651\\\",\\\"eventSeqId\\\":5827,\\\"eventType\\\":0,\\\"msgId\\\":\\\"c22232dc-f304-4c01-9f4a-9f23fd360073\\\",\\\"seqId\\\":1595,\\\"message\\\":\\\"AAAGyQABAAEAAQQSCiRmN2YxNGQ3Mi0yZGEyLTRjNTYtODgxOS1kOTZhYjliYmU5ZjkQAhq6AU1JR0lBa0lCZS1vRGczY2ZjRzFfT2FzQzdEd0FiWThjTUN0YWhuNVlpLWZjRFRxaW43MVpPanp6UDVKV0F1eVozN1pGcVNYV3pCTHczMmt3X0w0SjRMUVl0eUpkblhnQ1FnSHZtYklHOG9WcUJMSjJUaHFoQlBpMFN0SC10ZjBQLS1jTldob3I3c0FoaVZZcHJ0NXV6LW5USGVpTVoxbVRlM0RvcTlkYzNnd1ZxckVvZ1hiZGpIVVp2QSAEKRQ_C7FsAQAAMiRlNjllNzM4Zi1kZjQwLTQ0Y2YtODJkNS01YWRjZmM1NDM2NTE6FmYuYWFhMTEyQG1haWx0ZXN0MS5jb21C0wFNSUdiTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFqQTRHR0FBUUJLVFRlTVU1UmNENlduTUdncEVmZ2RlRVZjM3VhWmN0TjJ1WEJSQ28waFFfR1VONm14V2NoRWE3b2NiOWpPUGI5NFFJUnYybWhrbmczRmMzOHVnTVZTZ1FBa1FYQmd5bUFSZVJyWnltWjNCZmRHaUo4TVFIU0h0T0RXRjhnOHVNbktjYWxSRjdHR2NvdDM3QUFWTkZSUFQwV0hCSEZtM1VqeDJENjhYVkVTeVRUUnVVShRhLmZvcm1AbWFpbHRlc3QxLmNvbVLTAU1JR2JNQkFHQnlxR1NNNDlBZ0VHQlN1QkJBQWpBNEdHQUFRQWJRVnZpSXhIRE1XODRmTmVCaTZoUzN1STg4NWJVTmRQaVJDOVo3NTNQZHJDLU4tY2NOSEUxTXNZdXdRYVE0dVFadzZOYm5kbEpsM2VDNnlSSnhDSTdnUUE2X2tReTVUd3NwRVE5Y2dKeDdVTFNRSlMyNk9ldC1haXFVNGt3NWFLSWE1OHdZcXJiZExMOU1DV3FYRHg3WXhZOUhhaW13WlVXOW1YTnB1ekdad0tUS2tqgQJ7ImZyb20iOiJmLmFhYTExMkBtYWlsdGVzdDEuY29tIiwibXNnSWQiOiJjMjIyMzJkYy1mMzA0LTRjMDEtOWY0YS05ZjIzZmQzNjAwNzMiLCJzZXNzaW9uRXh0RGF0YSI6IntcImF2YXRhckhvc3RcIjpcImh0dHA6Ly9tc2dzZWFsLnN5c3Rvb250ZXN0LmNvbS90ZW1haWxpbWFnZXMvXCIsXCJjb250YWN0VHlwZVwiOjUsXCJuYW1lXCI6XCLmtYvor5UyXCJ9Iiwic3RvcmVUeXBlIjoxLCJ0byI6ImEuZm9ybUBtYWlsdGVzdDEuY29tIiwidHlwZSI6IjAifXIcbXNnc2VhbC5zeXN0b29udGVzdC5jb206ODA5OUFBQUFRd0FBQUVBQUFBRndBQUFCYndNQVpLY2Z2U1Fac1BCQXU3TnU1VHl2eUdCWXBvNExhaGFjeDZOc3VqaTdWUkZWdXA5MENKcTRJX3RnLVZBMTRPelJPSGRVN0VzeVZ1VDBxdF9PQkt3ZkNyLWloNmUwVDZ3eGo2WFZyU3V3dnB0VEg5WUdjbWVHbDhKckxCbnRsNUV0bHNma2hjSGxILWZEcDgtcmdJNU9QT3B0NTdYNi1xd1IyMU9wY2I3RnliWGFKbFJKNU4yTTk4YWFiY2JSYkdtZ2g1ZTVPTk1MWm8tYmU3aHhwbm95M1RyVWxPUkc1RE02cnFpSjBfV3JUNXBHVVNaVXlKcEYxUng2dzJHZEhBWTFlbFV6UXBCanZ4Y3pUb09mc05BZkJXc2VKOThiSlQ0U1hUMUo2VUN3N0lVZnJCQzZSQ21LREpldzdmdFd2MmlOb2N0clFvWGItY0tlaFU2STN3TXhvMmZuemRKTDVNbHRtVnBPRVFScmlGWWxwcGVzTHVobnhVMUlWY2huOEQ1ck1OczB1dTdBVG1qcmRvNjB6WURCVUJOSjc2U3V2c2EtMERzT2dnSjlLZ184VjJoQjE1akgxSWJkcmp0LXQxeU51NnJnemV1U2o2TXMzYTFjRG5xT1NJZTlaZGhtUDUzOWF3eXF1WExTVGJOTjZERzAwSFEtblQ1Z2NJOUotamZFeEF6NW5BQ2J3ckdsZm53clNBZVhVWGdjbWVHYW41NDczTHAtcDNMR09ZaDVKeDNOQzBndl85V0R2dERfaGczR2pYMl9lTUNqN1owVlhlTlF3VjktbWhnWno5OVVDTXNncnRKT0dhemdWMmNYWUVvc1NFTjJ1ZW1GYk5Lb1U2VzhrZVFXUkpYZjNBNFZpbGRaQ3gwM0YwOA\\u003d\\u003d\\\",\\\"from\\\":\\\"f.aaa112@mailtest1.com\\\",\\\"to\\\":\\\"a.form@mailtest1.com\\\",\\\"timestamp\\\":1566338400248,\\\"unread\\\":1939,\\\"unreadAt\\\":0}\"}";
    MessageHandler messageHandler = new MessageHandler(producer, gatewayLocator,
        properties.getRocketmq().getPushTopic(), properties.getRocketmq().getPushTag(),
        new PacketTypeJudge(null), t -> {
    }, new NotificationMessageFactory());
    messageHandler.onMessageReceived(message);


  }

}