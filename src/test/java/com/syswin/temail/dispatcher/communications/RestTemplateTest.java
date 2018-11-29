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
import com.syswin.temail.dispatcher.DispatcherApplication;
import com.syswin.temail.dispatcher.request.controller.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


@Slf4j
@SpringBootTest(classes = {DispatcherApplication.class},
    properties = {
    "app.httpClient.pool.maxTotal=3000",
    "app.httpClient.pool.defaultMaxPerRoute=500",
    "app.httpClient.pool.connectionRequestTimeout=3000",
    "app.httpClient.pool.connection.connectTimeout=3000",
    "app.httpClient.pool.connection.readTimeout=3000"
    })
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class RestTemplateTest {

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().dynamicPort());

  private static final Gson GSON = new Gson();

  private static String baseUrl = "";

  @Autowired
  private RestTemplate restTemplate;

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