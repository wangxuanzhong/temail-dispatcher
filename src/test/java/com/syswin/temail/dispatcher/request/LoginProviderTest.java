package com.syswin.temail.dispatcher.request;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.entity.CDTPPacket;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

@RunWith(SpringRestPactRunner.class)
@PactBroker(host = "172.28.50.206", port = "88")
@Provider("temail-dispatcher")
@SpringBootTest(webEnvironment = DEFINED_PORT, properties = "server.port=8081")
@ActiveProfiles("dev")
public class LoginProviderTest {
  @TestTarget
  public final Target target = new HttpTarget(8081);
  private final String unsignedBytes = "abc";
  private final String signature = "signed-abc";


  private final String sender = "jack@t.email";
  private final String receiver = "sean@t.email";
  private final String message = "hello world";
  private final String deviceId = uniquify("deviceId");

  private static final String ackMessage = "Sent ackMessage";

  private final Gson gson = new Gson();

  private final CDTPPacketTrans   cdtpPacketTrans = singleChatPacket(sender, receiver, message, deviceId);


  @MockBean
  private AuthService authService;


  @MockBean
  private PackageDispatcher packageDispatcher;

  @State("User sean is registered")
  public void userIsRegistered() {
    when(authService.verify("sean@t.email", unsignedBytes, signature)).thenReturn(ResponseEntity.ok(Response.ok("Success")));
  }

  @State("User jack is not registered")
  public void userNotRegistered() {
    when(authService.verify("jack@t.email", unsignedBytes, signature)).thenReturn(new ResponseEntity<>(Response.failed(FORBIDDEN), FORBIDDEN));
  }

  @State("User mike is registered, but server is out of work")
  public void serverOutOfWork() {
    when(authService.verify("mike@t.email", unsignedBytes, signature)).thenThrow(RestClientException.class);
  }

  @State("dispatch user request")
  public void   dispatchUserRequest() {
    when(packageDispatcher.dispatch(cdtpPacketTrans)).thenReturn((ResponseEntity<String>)ResponseEntity.ok(gson.toJson(Response.ok(OK, ackPayload()))));

  }

  // 创建单聊消息体
  public  CDTPPacketTrans singleChatPacket(String sender, String recipient, String message, String deviceId) {

    CDTPPacketTrans cdtpPacketTrans  = new  CDTPPacketTrans();
    short CDTP_VERSION = 1;
    cdtpPacketTrans.setCommandSpace((short) 1);
    cdtpPacketTrans.setCommand((short)1);
    cdtpPacketTrans.setVersion(CDTP_VERSION);

    CDTPPacketTrans.Header   cdtpPacketTransHeader  = new  CDTPPacketTrans.Header();
    cdtpPacketTransHeader.setSignatureAlgorithm(1);
    cdtpPacketTransHeader.setSignature("sign");
    cdtpPacketTransHeader.setDataEncryptionMethod(0);
    cdtpPacketTransHeader.setTimestamp(1535713173935L);
    cdtpPacketTransHeader.setPacketId("pkgId");
    cdtpPacketTransHeader.setDeviceId("deviceId_5514");
    cdtpPacketTransHeader.setSender(sender);
    cdtpPacketTransHeader.setReceiver(recipient);
    cdtpPacketTransHeader.setSenderPK("SenderPK123");
    cdtpPacketTransHeader.setReceiverPK("ReceiverPK456");
    Map<String, Object> extraData = new HashMap<>();
    extraData.put("from", sender);
    extraData.put("to", recipient);
    extraData.put("storeType", "2");
    extraData.put("type", "0");
    extraData.put("msgId", "4298F38F87DC4775B264A3753E77B443");
    cdtpPacketTransHeader.setExtraData(gson.toJson(extraData));
    cdtpPacketTrans.setHeader(cdtpPacketTransHeader);
//    String  messageStr = Base64.getEncoder().encodeToString(message.getBytes());
    cdtpPacketTrans.setData("aGVsbG8gd29ybGQ=");
    System.err.println(cdtpPacketTrans);
    return cdtpPacketTrans;
  }


  @NotNull
  private CDTPPacket ackPayload() {

    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace((short)1);
    payload.setCommand((short)1);
    payload.setData(gson.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }



}
