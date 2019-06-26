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

package com.syswin.temail.dispatcher.communications;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherApplication;
import com.syswin.temail.dispatcher.request.controller.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_METHOD_FAILURE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;


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

    stubFor(any(urlEqualTo("/error_namespace_2")).willReturn(
        aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
            .withStatus(SC_METHOD_FAILURE)
            .withBody(GSON.toJson(Response.failed(HttpStatus.METHOD_NOT_ALLOWED, "用户信息不存在或者不正确")))));

    baseUrl = "http://localhost:" + WIRE_MOCK_RULE.port();
  }

  @Test
  public void highPerformance4PoolBuilded() {
    int taskesPerThread = 5;
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    AtomicInteger restTimes = new AtomicInteger(Runtime.getRuntime().availableProcessors() * taskesPerThread);
    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          for (int j = 0; j < taskesPerThread; j++) {
            actionOneRequest(baseUrl + "/path_namespace_1", t -> {
              restTimes.decrementAndGet();
              assertThat(t.getStatusCode()).isEqualTo(OK);
            });
          }
        }
      });
    }
    Awaitility.waitAtMost(5000, TimeUnit.MILLISECONDS).until(() -> restTimes.get() == 0);
  }

  @Test
  public void getEntityWithNon2XXStatus() {
    this.actionOneRequest(baseUrl + "/error_namespace_2", t -> {
      assertThat(t.getStatusCode()).isEqualTo(HttpStatus.METHOD_FAILURE);
      assertThat(((Response) (t.getBody())).getCode()).isEqualTo(405);
      assertThat(((Response) (t.getBody())).getMessage()).isEqualTo("用户信息不存在或者不正确");
    });
  }

  @Test(expected = ResourceAccessException.class)
  public void throwExceptionIfAccessNotExistedResource() {
    this.actionOneRequest("http://not.existed.host/null/resource", t -> {
    });
  }

  private void actionOneRequest(String url, Consumer<ResponseEntity> consumer) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);
    HttpEntity entity = new HttpEntity(headers);
    ResponseEntity<Response> response = restTemplate.exchange(url, HttpMethod.GET, entity, Response.class);
    consumer.accept(response);
  }

}