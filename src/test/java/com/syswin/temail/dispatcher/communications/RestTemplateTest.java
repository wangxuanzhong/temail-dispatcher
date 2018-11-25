package com.syswin.temail.dispatcher.communications;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.controller.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("dev")
public class RestTemplateTest {

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().dynamicPort());

  private static final Gson GSON = new Gson();

  private static String baseUrl = "";

  @BeforeClass
  public static void beforeClass() {
    stubFor(any(urlEqualTo("/path_namespace_1")).willReturn(
        aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
            .withStatus(SC_OK)
            .withBody(GSON.toJson(Response.ok()))));

    stubFor(any(urlEqualTo("/path_namespace_2")).willReturn(
        aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
            .withStatus(SC_OK)
            .withBody(GSON.toJson(Response.ok()))));

    baseUrl = "http://localhost:" + WIRE_MOCK_RULE.port();
  }

  @Test
  public void highPerformance4PoolBuilded() {
    RestTemplate restTemplate = new RestTemplateConfig().restTemplate();
    ExecutorService executorService = Executors.newFixedThreadPool(200);
    AtomicInteger restTimes = new AtomicInteger(Runtime.getRuntime().availableProcessors()*100);
    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          for (int j = 0; j < 100; j++) {
            actionOneRequest(restTemplate,restTimes);
          }
        }
      });
    }
    Awaitility.waitAtMost(10000, TimeUnit.MILLISECONDS).until(() -> restTimes.get() == 0);
  }

  private void actionOneRequest(RestTemplate restTemplate, AtomicInteger restTimes) {
    String url = baseUrl + "/path_namespace_1";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);
    HttpEntity entity = new HttpEntity(headers);

    ResponseEntity<Response> response = restTemplate.
        exchange(url, HttpMethod.GET, entity, Response.class);
    assertThat(response.getStatusCode()).isEqualTo(OK);
    restTimes.decrementAndGet();
  }

}