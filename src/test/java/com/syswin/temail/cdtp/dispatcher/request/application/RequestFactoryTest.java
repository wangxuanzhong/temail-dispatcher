package com.syswin.temail.cdtp.dispatcher.request.application;

import static com.seanyinx.github.unit.scaffolding.Randomness.nextInt;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.cdtp.dispatcher.request.application.RequestFactory.BODY_EXCLUSIVE_GSON;
import static com.syswin.temail.cdtp.dispatcher.request.application.RequestFactory.CDTP_HEADER;
import static java.util.Arrays.asList;
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
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.cdtp.dispatcher.request.exceptions.TeMailUnsupportedCommandException;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;

public class RequestFactoryTest {

  private static final Map<String, Object> bodyJson = new Gson().fromJson("{\n"
      + "  \"foo\": \"bar\",\n"
      + "  \"hello\": \"world\"\n"
      + "}", new TypeToken<Map<String, Object>>() {
  }.getType());

  private final DispatcherProperties properties = new DispatcherProperties();
  private final Request request = new Request();
  private final String baseUrl = "http://localhost:" + nextInt(1000);
  private final String command = uniquify("command");
  private final CDTPPackage cdtpPackage = initCDTPPackage();

  private final HttpMethod[] methods = {GET, POST, PUT, DELETE};

  private final RequestFactory requestFactory = new RequestFactory(properties);

  @Before
  public void setUp() {
    request.setUrl(baseUrl + "/" + command);

    properties.setCmdRequestMap(singletonMap(command, request));
    cdtpPackage.getData().setCommand(command);
  }

  @Test
  public void plainRequest() {
    for (HttpMethod method : methods) {
      request.setMethod(method);

      TeMailRequest temailRequest = requestFactory.toRequest(cdtpPackage);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(BODY_EXCLUSIVE_GSON.toJson(cdtpPackage))));

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithHeader() {
    ImmutableMap<String, List<String>> headers = ImmutableMap.of(
        uniquify("headerName1"), singletonList("headerValue1"),
        uniquify("headerName2"), asList("headerValue2", "headerValue3"));

    for (HttpMethod method : methods) {
      request.setMethod(method);

      cdtpPackage.getData()
          .getParams()
          .getHeader()
          .putAll(headers);

      TeMailRequest temailRequest = requestFactory.toRequest(cdtpPackage);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(BODY_EXCLUSIVE_GSON.toJson(cdtpPackage))));

      assertThat(temailRequest.entity().getHeaders())
          .containsAllEntriesOf(headers);

      assertThat(temailRequest.entity().getBody()).isNull();
    }
  }

  @Test
  public void requestWithBody() {
    cdtpPackage.getData().getParams().setBody(bodyJson);

    for (HttpMethod method : new HttpMethod[]{POST, PUT}) {
      request.setMethod(method);

      TeMailRequest temailRequest = requestFactory.toRequest(cdtpPackage);

      assertThat(temailRequest.url()).isEqualTo(request.getUrl());
      assertThat(temailRequest.method()).isEqualTo(request.getMethod());
      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CONTENT_TYPE, singletonList(APPLICATION_JSON_UTF8_VALUE)));

      assertThat(temailRequest.entity().getHeaders())
          .contains(new SimpleEntry<>(CDTP_HEADER, singletonList(BODY_EXCLUSIVE_GSON.toJson(cdtpPackage))));

      assertThat(temailRequest.entity().getBody()).isEqualTo(bodyJson);
    }
  }

  @Test(expected = TeMailUnsupportedCommandException.class)
  public void blowsUpWhenCommandIsNotMapped() {
    cdtpPackage.getData().setCommand(uniquify("command"));
    requestFactory.toRequest(cdtpPackage);
  }

  @Test(expected = TeMailUnsupportedCommandException.class)
  public void blowsUpWhenMethodIsNotSupported() {
    request.setMethod(TRACE);
    requestFactory.toRequest(cdtpPackage);
  }

  private CDTPPackage initCDTPPackage() {
    CDTPPackage cdtpPackage = new CDTPPackage();
    cdtpPackage.setCommand(nextInt(10));
    cdtpPackage.setVersion(nextInt(10));
    cdtpPackage.setAlgorithm(nextInt(10));
    cdtpPackage.setSign("sign");
    cdtpPackage.setDem(nextInt(10));
    cdtpPackage.setTimestamp(System.currentTimeMillis());
    cdtpPackage.setPkgId("pkgId");
    cdtpPackage.setFrom("yaohuacheng@syswin.com");
    cdtpPackage.setTo("yaohuacheng@syswin.com");
    cdtpPackage.setSenderPK("SenderPK(");
    cdtpPackage.setReceiverPK("ReceiverPK(");

    CDTPBody cdtpBody = new CDTPBody();

    CDTPBody.CDTPParams params = new CDTPBody.CDTPParams();

    params.setHeader(new LinkedMultiValueMap<>());
    params.setQuery(new LinkedMultiValueMap<>());
    cdtpBody.setParams(params);
    cdtpPackage.setData(cdtpBody);
    return cdtpPackage;
  }
}
