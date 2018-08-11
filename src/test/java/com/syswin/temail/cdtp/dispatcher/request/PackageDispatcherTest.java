package com.syswin.temail.cdtp.dispatcher.request;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.cdtp.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody.CDTPParams;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

public class PackageDispatcherTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8081);
  private static final String COMMAND1 = uniquify("command1");
  private static final String COMMAND2 = uniquify("command2");

  private static final String bodyJson = "{\n"
      + "  \"foo\": \"bar\",\n"
      + "  \"hello\": \"world\"\n"
      + "}";

  private static final String responseBody1 = uniquify("response1");
  private static final String responseBody2 = uniquify("response2");


  private final String baseUrl = "http://localhost:" + wireMockRule.port();

  private final RestTemplate restTemplate = new RestTemplate();
  private final DispatcherProperties properties = new DispatcherProperties();
  private final Request request = new Request();

  private final CDTPPackage<CDTPBody> cdtpPackage = initCDTPPackage();
  private final HttpMethod[] methods = {GET, POST, PUT, DELETE};

  private final ImmutableMap<String, List<String>> headers = ImmutableMap.of(
      "h1", singletonList("v1"),
      "h2", asList("v21", "v22"));

  private final ImmutableMap<String, List<String>> queries = ImmutableMap.of(
      "q1", singletonList("v1"),
      "q2", asList("v21", "v22"));

  private final PackageDispatcher packageDispatcher = new PackageDispatcher(properties, restTemplate);

  @BeforeClass
  public static void beforeClass() {
    stubFor(any(urlEqualTo("/" + COMMAND1 + "?q1=v1&q2=v21&q2=v22"))
        .withHeader("h1", equalTo("v1"))
        .withHeader("h2", containing("v21"))
        .withHeader("h2", containing("v22"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
                .withStatus(SC_OK)
                .withBody(responseBody1)));

    stubFor(any(urlEqualTo("/" + COMMAND2 + "?q1=v1&q2=v21&q2=v22"))
        .withHeader("h1", equalTo("v1"))
        .withHeader("h2", containing("v21"))
        .withHeader("h2", containing("v22"))
        .withRequestBody(equalToJson(bodyJson))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
                .withStatus(SC_OK)
                .withBody(responseBody2)));
  }

  @Before
  public void setUp() {
    properties.setCmdRequestMap(ImmutableMap.of(COMMAND1, request, COMMAND2, request));
  }

  @Test
  public void requestWithoutBody() {
    cdtpPackage.getData().setCommand(COMMAND1);
    request.setUrl(baseUrl + "/" + COMMAND1);

    CDTPParams params = cdtpPackage.getData().getParams();
    for (HttpMethod method : methods) {
      request.setMethod(method);

      params.getHeader().putAll(headers);
      params.getQuery().putAll(queries);

      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(cdtpPackage);

      assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
      assertThat(responseEntity.getBody()).isEqualTo(responseBody1);
    }
  }

  @Test
  public void requestWithBody() {
    cdtpPackage.getData().setCommand(COMMAND2);
    request.setUrl(baseUrl + "/" + COMMAND2);

    CDTPParams params = cdtpPackage.getData().getParams();
    for (HttpMethod method : new HttpMethod[]{POST, PUT}) {
      request.setMethod(method);

      params.getHeader().putAll(headers);
      params.getQuery().putAll(queries);
      params.setBody(bodyJson);

      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(cdtpPackage);

      assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
      assertThat(responseEntity.getBody()).isEqualTo(responseBody2);
    }
  }

  private CDTPPackage<CDTPBody> initCDTPPackage() {
    CDTPPackage<CDTPBody> cdtpPackage = new CDTPPackage<>();
    cdtpPackage.setCommand(1);
    cdtpPackage.setVersion(1);
    cdtpPackage.setAlgorithm(1);
    cdtpPackage.setSign("sign");
    cdtpPackage.setDem(1);
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
