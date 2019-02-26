package com.syswin.temail.dispatcher.request.application;

import static com.seanyinx.github.unit.scaffolding.Randomness.nextInt;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.dispatcher.request.application.RequestFactory.CDTP_HEADER;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.TRACE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.dispatcher.valid.PacketValidJudge;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import com.syswin.temail.ps.common.packet.PacketUtil;
import java.util.AbstractMap.SimpleEntry;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@RunWith(JUnit4.class)
public class RequestFactoryTest {

  private static Gson gson = new Gson();
  private final DispatcherProperties properties = new DispatcherProperties();
  private final Request request = new Request();
  private final String baseUrl = "http://localhost:" + nextInt(1000);
  private final HttpMethod[] methods = {GET, POST, PUT, DELETE};
  private final PacketTypeJudge packetTypeJudge = new PacketTypeJudge(null);
  private CommandAwarePacketUtil packetUtil = new CommandAwarePacketUtil(packetTypeJudge);
  private final RequestFactory requestFactory = new RequestFactory(properties, packetUtil);

  private final CommandAwarePacketUtil commandAwarePacketUtil = new CommandAwarePacketUtil(packetTypeJudge);
  private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
  private final AuthService authService = new AuthService(restTemplate,
      properties, commandAwarePacketUtil, new PacketValidJudge(properties));

  private static CDTPPacket initCDTPPacketTrans() {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace((short) 0xA);
    packet.setCommand((short) 0xF0F);
    packet.setVersion((short) 1);

    CDTPHeader header = new CDTPHeader();
    header.setSignatureAlgorithm(1);
    header.setSignature("sign");
    header.setDataEncryptionMethod(1);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId("pkgId");
    header.setSender("yaohuacheng@syswin.com");
    header.setSenderPK("SenderPK(");
    header.setReceiver("yaohuacheng@syswin.com");
    header.setReceiverPK("ReceiverPK(");
    packet.setHeader(header);

    CDTPParams cdtpParams = new CDTPParams();
    packet.setData(gson.toJson(cdtpParams).getBytes());

    return packet;
  }

  private static CDTPPacket initCDTPPacket() {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace((short) 0xA);
    packet.setCommand((short) 0xF0F);
    packet.setVersion((short) 1);

    CDTPHeader header = new CDTPHeader();
    header.setSignatureAlgorithm(1);
    header.setSignature("sign");
    header.setDataEncryptionMethod(1);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId("pkgId");
    header.setSender("yaohuacheng@syswin.com");
    header.setSenderPK("SenderPK");
    header.setReceiver("yaohuacheng@syswin.com");
    header.setReceiverPK("ReceiverPK");
    packet.setHeader(header);
    return packet;
  }

  @Before
  public void setUp() {
    String urlPath = "test";
    request.setUrl(baseUrl + "/" + urlPath);

    String command = "A0F0F";
    properties.setCmdMap(singletonMap(command, request));
  }

  @Test
  public void plainRequest() {
    CDTPPacket packet = initCDTPPacketTrans();
    for (HttpMethod method : methods) {
      request.setMethod(method);

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet.getHeader()))));

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithHeader() {
    CDTPPacket packet = initCDTPPacketTrans();
    Map<String, String> headers = ImmutableMap.of(
        uniquify("headerName1"), "headerValue1",
        uniquify("headerName2"), "headerValue2");
    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setHeader(headers);
    packet.setData(gson.toJson(cdtpParams).getBytes());

    for (HttpMethod method : methods) {
      request.setMethod(method);

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet.getHeader()))));

      String[] headerNames = new String[headers.keySet().size()];
      assertThat(temailRequest.entity().getHeaders())
          .containsKeys(headers.keySet().toArray(headerNames));

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithPathVariable() {
    request.setUrl(baseUrl + "/{Name1}/{Name2}");

    CDTPPacket packet = initCDTPPacketTrans();
    Map<String, Object> pathVariables = ImmutableMap.of(
        "Name1", "Value1",
        "Name2", "Value2");

    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setPath(pathVariables);
    packet.setData(gson.toJson(cdtpParams).getBytes());

    for (HttpMethod method : methods) {
      request.setMethod(method);

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(baseUrl + "/Value1/Value2");
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet.getHeader()))));

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithQueries() {
    CDTPPacket packet = initCDTPPacketTrans();
    Map<String, String> query = ImmutableMap.of(
        "Name", "Value1,Value2");

    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setQuery(query);
    packet.setData(gson.toJson(cdtpParams).getBytes());

    for (HttpMethod method : methods) {
      request.setMethod(method);

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(baseUrl + "/test?Name=Value1,Value2");
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithBody() {
    CDTPPacket packet = initCDTPPacketTrans();
    final Map<String, Object> body = ImmutableMap.of(
        "foo", "bar",
        "hello", "world");
    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setBody(body);

    packet.setData(gson.toJson(cdtpParams).getBytes());

    for (HttpMethod method : new HttpMethod[]{POST, PUT}) {
      request.setMethod(method);

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CONTENT_TYPE, singletonList(APPLICATION_JSON_UTF8_VALUE)));

      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet.getHeader()))));

      assertThat(temailRequest.entity().getBody()).isEqualTo(body);
    }
  }

  @Test
  public void requestWithSingleChat() {
    CDTPPacket packet = initCDTPPacketTrans();
    packet.setCommandSpace((short) 1);
    packet.setCommand((short) 1);

    Map<String, Object> extraData = new HashMap<>();
    CDTPHeader header = packet.getHeader();
    header.setExtraData(gson.toJson(extraData));
    header.setDataEncryptionMethod(4);
    packet.setData("This is Encrypt Data".getBytes());

    request.setMethod(POST);
    properties.setCmdMap(singletonMap("10001", request));

    TemailRequest temailRequest = requestFactory.toRequest(packet);

    assertThat(temailRequest.url()).isEqualTo(request.getUrl());
    assertThat(temailRequest.method()).isEqualTo(request.getMethod());
    HttpEntity<Map<String, Object>> entity = temailRequest.entity();
    assertThat(entity.getHeaders())
        .contains(new SimpleEntry<>(CONTENT_TYPE, singletonList(APPLICATION_JSON_UTF8_VALUE)));

    assertThat(entity.getHeaders())
        .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(header))));

    Map<String, Object> testMap = new HashMap<>(3);
    testMap.put("meta", packet.getHeader());
    testMap.put("msgData", Base64.getUrlEncoder().encodeToString(packet.getData()));

    Map<String, Object> bodyMap = entity.getBody();
    assertThat(bodyMap)
        .containsAllEntriesOf(testMap);
  }

  @Test
  public void requestWithGroupChatAndGroupPacketEnabled() {
    CDTPPacket packet = initCDTPPacket();
    packet.setCommandSpace((short) 2);
    packet.setCommand((short) 1);

    Map<String, Object> bodyData = new HashMap<>();
    bodyData.put("from", "sender@t.email");
    bodyData.put("to", "group@t.email");
    bodyData.put("message", "对称加密报文base64");
    bodyData.put("type", "0");
    bodyData.put("msgId", "消息ID");
    String paramString = gson.toJson(new CDTPParams(bodyData));
    packet.setData(paramString.getBytes());

    byte[] bytes = PacketUtil.pack(packet, true);
    CDTPPacket newPacket = new CDTPPacket(packet);
    newPacket.setData(bytes);

    request.setMethod(POST);
    properties.setCmdMap(singletonMap("20001", request));

    TemailRequest temailRequest = requestFactory.toRequest(newPacket);

    assertThat(temailRequest.url()).isEqualTo(request.getUrl());
    assertThat(temailRequest.method()).isEqualTo(request.getMethod());
    HttpEntity<Map<String, Object>> entity = temailRequest.entity();
    assertThat(entity.getHeaders())
        .contains(new SimpleEntry<>(CONTENT_TYPE, singletonList(APPLICATION_JSON_UTF8_VALUE)));

    CDTPHeader header = packet.getHeader();
    assertThat(entity.getHeaders())
        .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(header))));

    Map<String, Object> bodyMap = entity.getBody();
    assertThat(bodyMap)
        .containsAllEntriesOf(bodyData);
    assertThat(bodyMap).containsKeys("meta", "packet");
  }

  @Test(expected = DispatchException.class)
  public void blowsUpWhenCommandIsNotMapped() {
    CDTPPacket packet = initCDTPPacketTrans();
    packet.setCommand((short) 0xFF);
    requestFactory.toRequest(packet);
  }

  @Test(expected = DispatchException.class)
  public void blowsUpWhenMethodIsNotSupported() {
    CDTPPacket packet = initCDTPPacketTrans();
    request.setMethod(TRACE);
    requestFactory.toRequest(packet);
  }

  @Test
  public void verifyPramsIfGroupType(){
    CDTPPacket cdtpPacket = initCDTPPacket();
    cdtpPacket.setCommandSpace(CommandSpaceType.GROUP_MESSAGE_CODE);
    cdtpPacket.setCommand((short) 1);
    request.setMethod(POST);
    properties.setCmdMap(singletonMap("20001", request));

    Map<String, Object> bodyData = new HashMap<>();
    bodyData.put("from", "sender@t.email");
    bodyData.put("to", "group@t.email");
    bodyData.put("message", "对称加密报文base64");
    bodyData.put("type", "0");
    bodyData.put("msgId", "消息ID");
    String paramString = gson.toJson(new CDTPParams(bodyData));
    cdtpPacket.setData(paramString.getBytes());
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));

    TemailRequest request = requestFactory.toRequest(cdtpPacket);
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.PUBLIC_KEY)).isTrue();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.ALGORITHM)).isTrue();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.SIGNATURE)).isTrue();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.TE_MAIL)).isTrue();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.UNSIGNED_BYTES)).isTrue();
  }

  @Test
  public void noVerifyPramsIfGroupType(){
    CDTPPacket cdtpPacket = initCDTPPacket();
    cdtpPacket.setCommandSpace(CommandSpaceType.SINGLE_MESSAGE_CODE);
    cdtpPacket.setCommand((short) 1);
    request.setMethod(POST);
    properties.setCmdMap(singletonMap("10001", request));

    Map<String, Object> bodyData = new HashMap<>();
    bodyData.put("from", "sender@t.email");
    bodyData.put("to", "group@t.email");
    bodyData.put("message", "对称加密报文base64");
    bodyData.put("type", "0");
    bodyData.put("msgId", "消息ID");
    String paramString = gson.toJson(new CDTPParams(bodyData));
    cdtpPacket.setData(paramString.getBytes());
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));

    TemailRequest request = requestFactory.toRequest(cdtpPacket);
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.PUBLIC_KEY)).isFalse();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.ALGORITHM)).isFalse();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.SIGNATURE)).isFalse();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.TE_MAIL)).isFalse();
    assertThat(request.entity().getHeaders().containsKey(RequestFactory.UNSIGNED_BYTES)).isFalse();
  }

}
