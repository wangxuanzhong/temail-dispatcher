package com.syswin.temail.dispatcher.request.application;

import static com.seanyinx.github.unit.scaffolding.Randomness.nextInt;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.dispatcher.request.PacketMaker.initCDTPPackage;
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
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class RequestFactoryTest {

  private static final Map<String, Object> body = ImmutableMap.of(
      "foo", "bar",
      "hello", "world");

  private final DispatcherProperties properties = new DispatcherProperties();
  private final Request request = new Request();
  private final String baseUrl = "http://localhost:" + nextInt(1000);
  private final String command = uniquify("command");
  private final CDTPPacketTrans packet = initCDTPPackage();

  private final HttpMethod[] methods = {GET, POST, PUT, DELETE};

  private final RequestFactory requestFactory = new RequestFactory(properties);
  private Gson gson = new Gson();

  @Before
  public void setUp() {
    request.setUrl(baseUrl + "/" + command);

    properties.setCmdMap(singletonMap(command, request));
  }

  @Test
  public void plainRequest() {
    for (HttpMethod method : methods) {
      request.setMethod(method);

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet))));

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithHeader() {
    Map<String, String> headers = ImmutableMap.of(
        uniquify("headerName1"), "headerValue1",
        uniquify("headerName2"), "headerValue2");

    for (HttpMethod method : methods) {
      request.setMethod(method);
      CDTPParams cdtpParams = new CDTPParams();
      cdtpParams.setHeader(headers);
      packet.setData(gson.toJson(cdtpParams));

      TemailRequest temailRequest = requestFactory.toRequest(packet);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet))));

//      assertThat(temailRequest.entity().getHeaders())
//          .containsAllEntriesOf(headers);

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithBody() {
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
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(gson.toJson(packet))));

      assertThat(temailRequest.entity().getBody()).isEqualTo(body);
    }
  }

  @Test(expected = DispatchException.class)
  public void blowsUpWhenCommandIsNotMapped() {
//    packet.getData().setCommand(uniquify("command"));
    requestFactory.toRequest(packet);
  }

  @Test(expected = DispatchException.class)
  public void blowsUpWhenMethodIsNotSupported() {
    request.setMethod(TRACE);
    requestFactory.toRequest(packet);
  }

}
