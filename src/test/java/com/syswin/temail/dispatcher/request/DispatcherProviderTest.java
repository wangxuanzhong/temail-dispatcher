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

package com.syswin.temail.dispatcher.request;

import static com.syswin.temail.dispatcher.request.PacketMaker.loginPacket;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.request.application.DispAuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
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
@ActiveProfiles({"dev", "pact"})
public class DispatcherProviderTest {

  private static final String ackMessage = "Sent ackMessage";
  @TestTarget
  public final Target target = new HttpTarget(8081);
  private final String sender = "jack@t.email";
  private final String receiver = "sean@t.email";
  private final Gson gson = new Gson();

  private final PacketEncoder encoder = new PacketEncoder();
  private final CDTPPacket authPacket = loginPacket("", "deviceId");
  private final CDTPPacket cdtpPacket = privateMsgPacket(sender, receiver);


  @MockBean
  private DispAuthService dispAuthService;

  @MockBean
  private PackageDispatcher packageDispatcher;

  @State("User sean is registered")
  public void userIsRegistered() {
    authPacket.getHeader().setSender("sean@t.email");
    when(dispAuthService.verify(authPacket))
        .thenReturn(ResponseEntity.ok(Response.ok("Success")));
  }

  @State("User jack is not registered")
  public void userNotRegistered() {
    authPacket.getHeader().setSender("jack@t.email");
    when(dispAuthService.verify(authPacket))
        .thenReturn(new ResponseEntity<>(Response.failed(FORBIDDEN), FORBIDDEN));
  }

  @State("User mike is registered, but server is out of work")
  public void serverOutOfWork() {
    authPacket.getHeader().setSender("mike@t.email");
    when(dispAuthService.verify(authPacket)).thenThrow(RestClientException.class);
  }

  @State("dispatch user request")
  public void dispatchUserRequest() {
    when(dispAuthService.verify(cdtpPacket))
        .thenReturn(ResponseEntity.ok(Response.ok("Success")));

    when(packageDispatcher.dispatch(cdtpPacket))
        .thenReturn(ResponseEntity.ok(gson.toJson(Response.ok(OK, ackPayload()))));
  }

  private CDTPPacket privateMsgPacket(String sender, String recipient) {

    CDTPPacket cdtpPacketTrans = new CDTPPacket();
    short CDTP_VERSION = 1;
    cdtpPacketTrans.setCommandSpace((short) 1);
    cdtpPacketTrans.setCommand((short) 1);
    cdtpPacketTrans.setVersion(CDTP_VERSION);

    CDTPHeader cdtpPacketTransHeader = new CDTPHeader();
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
    cdtpPacketTrans.setData("hello world".getBytes());
    cdtpPacketTrans.setData(encoder.encode(cdtpPacketTrans));
    return cdtpPacketTrans;
  }

  @NotNull
  private CDTPPacket ackPayload() {

    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace((short) 1);
    payload.setCommand((short) 1);
    payload.setData(gson.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }
}
