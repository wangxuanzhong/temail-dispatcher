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
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpMethod;

@RunWith(JUnit4.class)
public class RequestFactoryTest {

  private static Gson gson = new Gson();
  private final DispatcherProperties properties = new DispatcherProperties();
  private final Request request = new Request();
  private final String baseUrl = "http://localhost:" + nextInt(1000);
  private final HttpMethod[] methods = {GET, POST, PUT, DELETE};
  private final RequestFactory requestFactory = new RequestFactory(properties);

  private static CDTPPacketTrans initCDTPPackage() {
    CDTPPacketTrans packet = new CDTPPacketTrans();
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
    packet.setData(gson.toJson(cdtpParams));

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
    CDTPPacketTrans packet = initCDTPPackage();
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
    CDTPPacketTrans packet = initCDTPPackage();
    Map<String, String> headers = ImmutableMap.of(
        uniquify("headerName1"), "headerValue1",
        uniquify("headerName2"), "headerValue2");
    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setHeader(headers);
    packet.setData(gson.toJson(cdtpParams));

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

    CDTPPacketTrans packet = initCDTPPackage();
    Map<String, Object> pathVariables = ImmutableMap.of(
        "Name1", "Value1",
        "Name2", "Value2");

    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setPath(pathVariables);
    packet.setData(gson.toJson(cdtpParams));

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
  public void requestWithBody() {
    CDTPPacketTrans packet = initCDTPPackage();
    final Map<String, Object> body = ImmutableMap.of(
        "foo", "bar",
        "hello", "world");
    CDTPParams cdtpParams = new CDTPParams();
    cdtpParams.setBody(body);

    packet.setData(gson.toJson(cdtpParams));

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
    CDTPPacketTrans packet = initCDTPPackage();
    packet.setCommandSpace((short) 1);
    packet.setCommand((short) 1);

    Map<String, Object> extraData = new HashMap<>();
    CDTPHeader header = packet.getHeader();
    header.setExtraData(gson.toJson(extraData));

    packet.setData("This is Encrypt Data");

    request.setMethod(POST);
    properties.setCmdMap(singletonMap("10001", request));

    TemailRequest temailRequest = requestFactory.toRequest(packet);

    assertThat(temailRequest.url()).isEqualTo(request.getUrl());
    assertThat(temailRequest.method()).isEqualTo(request.getMethod());
    assertThat(temailRequest.entity().getHeaders())
        .contains(new SimpleEntry<>(CONTENT_TYPE, singletonList(APPLICATION_JSON_UTF8_VALUE)));

    assertThat(temailRequest.entity().getHeaders())
        .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(header))));

    Map<String, Object> testMap = new HashMap<>(3);
    testMap.put("sender", header.getSender());
    testMap.put("receiver", header.getReceiver());
    testMap.put("msgData", packet.getData());

    Map<String, Object> bodyMap = (Map<String, Object>) temailRequest.entity().getBody();
    assertThat(bodyMap)
        .containsAllEntriesOf(testMap);
  }

  @Test(expected = DispatchException.class)
  public void blowsUpWhenCommandIsNotMapped() {
    CDTPPacketTrans packet = initCDTPPackage();
    packet.setCommand((short) 0xFF);
    requestFactory.toRequest(packet);
  }

  @Test(expected = DispatchException.class)
  public void blowsUpWhenMethodIsNotSupported() {
    CDTPPacketTrans packet = initCDTPPackage();
    request.setMethod(TRACE);
    requestFactory.toRequest(packet);
  }
}
