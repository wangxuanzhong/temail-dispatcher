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

package com.syswin.temail.dispatcher.notify;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocations;
import com.syswin.temail.dispatcher.request.application.SilentResponseErrorHandler;
import com.syswin.temail.dispatcher.request.controller.Response;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


@Ignore
public class DiscoveryConsumerTest extends ConsumerPactTestMk2 {

  private static final Gson gson = new Gson();
  private static final String path = "/locations";
  private static final String sean = "sean@t.email";
  private static final String jack = "jack@t.email";

  private static final String deviceId = "iOS-sean";
  private static final String mqTopic = "temail-gateway";
  private static final String mqTag = "gateway-localhost";
  private static final String gatewayHost = "localhost";
  private static final String processId = "12345";
  private final TemailAccountLocations locations = new TemailAccountLocations(location());

  private final RestTemplate restTemplate = new RestTemplateBuilder()
      .errorHandler(new SilentResponseErrorHandler())
      .build();

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    return pactDslWithProvider
        .given("Locate connection")
          .uponReceiving("locate connection by account")
          .method("GET")
          .path(path + "/" + sean)
          .willRespondWith()
          .status(200)
          .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
          .body(gson.toJson(Response.ok(OK, locations)))
        .given("Remote discovery service error")
          .uponReceiving("request to unavailable discovery service")
          .method("GET")
          .path(path + "/" + jack)
          .willRespondWith()
          .status(500)
          .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
          .body(gson.toJson(Response.failed(INTERNAL_SERVER_ERROR)))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path + "/{temail}";

    RemoteChannelStsLocator gatewayLocator = new RemoteChannelStsLocator(restTemplate, url);

    List<TemailAccountLocation> locations = gatewayLocator.locate(sean);

    assertThat(locations).hasSize(1);
    assertThat(locations.get(0)).isEqualToComparingFieldByField(this.locations.getStatuses().get(0));


    locations = gatewayLocator.locate(jack);
    assertThat(locations).isEmpty();


    gatewayLocator = new RemoteChannelStsLocator(restTemplate, "http://localhost:99");
    locations = gatewayLocator.locate(sean);

    assertThat(locations).isEmpty();
  }

  @Override
  protected String providerName() {
    return "temail-discovery";
  }

  @Override
  protected String consumerName() {
    return "temail-dispatcher";
  }

  @NotNull
  private List<TemailAccountLocation> location() {
    TemailAccountLocation status = new TemailAccountLocation(
        sean,
        deviceId,
        "",
        gatewayHost,
        processId,
        mqTopic,
        mqTag);

    return singletonList(status);
  }
}
