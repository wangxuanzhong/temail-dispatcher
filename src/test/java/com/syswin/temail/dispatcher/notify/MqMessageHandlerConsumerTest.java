package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;


public class MqMessageHandlerConsumerTest {

  private final static Gson gson = new Gson();
  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
  private final MQProducer producer = Mockito.mock(MQProducer.class);
  private final String recipient = "sean@t.email";
  private final CDTPPacketTrans payload = mqMsgPayload(recipient, "bonjour");
  private final GatewayLocator gatewayLocator = Mockito.mock(GatewayLocator.class);
  private byte[] currentMessage;
  private DispatcherProperties properties;

  public static CDTPPacketTrans mqMsgPayload(String recipient, String message) {
    Response<String> body = Response.ok(message);
    CDTPPacketTrans payload = new CDTPPacketTrans();
    payload.setCommandSpace((short) 3);
    payload.setCommand(NOTIFY_COMMAND);
    payload.setVersion(CDTP_VERSION);
    CDTPPacketTrans.Header header = new CDTPPacketTrans.Header();
    header.setReceiver(recipient);
    payload.setHeader(header);
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
    List<TemailAccountLocation> locations = new ArrayList<>();
    when(gatewayLocator.locate(recipient)).thenReturn(locations);
    MessageHandler messageHandler = new MessageHandler(producer, gatewayLocator);

    String msg = new String(currentMessage);
    messageHandler.onMessageReceived(msg);

//    verify(producer).send(argThat(matchesPayload(payload)));
  }

  public void setMessage(byte[] messageContents) {
    currentMessage = messageContents;
  }

  private ArgumentMatcher<CDTPPacketTrans> matchesPayload(CDTPPacketTrans payload) {
    return packet -> gson.toJson(payload)
        .equals(gson.toJson(packet));
  }

  private void setStringIfNotNull(PactDslJsonBody header, String key, String value) {
    if (value != null) {
      header.stringValue(key, value);
    }
  }

  private PactDslJsonBody packetJson(CDTPPacketTrans cdtpPacketTrans) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.numberValue("commandSpace", cdtpPacketTrans.getCommandSpace());
    body.numberValue("command", cdtpPacketTrans.getCommand());
    body.numberValue("version", cdtpPacketTrans.getVersion());
    body.stringValue("data", cdtpPacketTrans.getData());

    PactDslJsonBody header = new PactDslJsonBody();
    setStringIfNotNull(header, "deviceId", cdtpPacketTrans.getHeader().getDeviceId());
    header.numberValue("signatureAlgorithm", cdtpPacketTrans.getHeader().getSignatureAlgorithm());
    setStringIfNotNull(header, "signature", cdtpPacketTrans.getHeader().getSignature());
    header.numberValue("dataEncryptionMethod", cdtpPacketTrans.getHeader().getDataEncryptionMethod());
    header.numberValue("timestamp", cdtpPacketTrans.getHeader().getTimestamp());
    setStringIfNotNull(header, "packetId", cdtpPacketTrans.getHeader().getPacketId());
    setStringIfNotNull(header, "sender", cdtpPacketTrans.getHeader().getSender());
    setStringIfNotNull(header, "senderPK", cdtpPacketTrans.getHeader().getSenderPK());
    setStringIfNotNull(header, "receiver", cdtpPacketTrans.getHeader().getReceiver());
    setStringIfNotNull(header, "receiverPK", cdtpPacketTrans.getHeader().getReceiverPK());
    setStringIfNotNull(header, "at", cdtpPacketTrans.getHeader().getAt());
    setStringIfNotNull(header, "topic", cdtpPacketTrans.getHeader().getTopic());
    setStringIfNotNull(header, "extraData", cdtpPacketTrans.getHeader().getExtraData());
    body.object("header", header);
    return body;
  }

}