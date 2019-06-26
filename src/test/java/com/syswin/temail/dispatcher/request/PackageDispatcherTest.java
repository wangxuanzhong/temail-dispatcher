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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.request.application.CommandAwarePacketUtil;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.application.RequestFactory;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PackageDispatcherTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
  private static final String COMMAND1 = "command1";
  private static final String COMMAND2 = "command2";


  private static final String responseBody1 = uniquify("response1");
  private static final String responseBody2 = uniquify("response2");
  private static final Map<String, Object> body = ImmutableMap.of(
      "foo", "bar",
      "hello", "world");
  private static Gson gson = new Gson();
  private final String baseUrl = "http://localhost:" + wireMockRule.port();
  private final RestTemplate restTemplate = new RestTemplate();
  private final DispatcherProperties properties = new DispatcherProperties();
  private final Request request = new Request();
  private final HttpMethod[] methods = {GET, POST, PUT, DELETE};
  private final Map<String, String> headers = ImmutableMap.of(
      "h1", "v1",
      "h2", "v21");
  private final Map<String, String> queries = ImmutableMap.of(
      "q1", "v1",
      "q2", "v22");
  private final PackageDispatcher packageDispatcher = new PackageDispatcher(properties, restTemplate,
      new RequestFactory(properties,new CommandAwarePacketUtil(new PacketTypeJudge(null))));

  @BeforeClass
  public static void beforeClass() {
    stubFor(any(urlEqualTo("/" + COMMAND1 + "?q1=v1&q2=v22"))
        .withHeader("h1", equalTo("v1"))
        .withHeader("h2", containing("v21"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(responseBody1)));

    stubFor(any(urlEqualTo("/" + COMMAND2 + "?q1=v1&q2=v22"))
        .withHeader("h1", equalTo("v1"))
        .withHeader("h2", containing("v21"))
        .withRequestBody(equalToJson(gson.toJson(body)))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(responseBody2)));
  }

  static CDTPPacket initCDTPPackage() {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace((short) 0x0A);
    packet.setCommand((short) 0x0F01);
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

    return packet;
  }

  @Before
  public void setUp() {
    properties.setCmdMap(ImmutableMap.of("A0F01", request, "A0F02", request));
  }

  @Test
  public void requestWithoutBody() {
    CDTPPacket packet = initCDTPPackage();
    packet.setCommandSpace((short) 0x0A);
    packet.setCommand((short) 0x0F01);
    request.setUrl(baseUrl + "/" + COMMAND1);

    CDTPParams params = new CDTPParams();
    params.setHeader(headers);
    params.setQuery(queries);
    packet.setData(gson.toJson(params).getBytes());

    for (HttpMethod method : methods) {
      request.setMethod(method);

      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(packet);

      assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
      assertThat(responseEntity.getBody()).isEqualTo(responseBody1);
    }
  }

  @Test
  public void requestWithBody() {
    CDTPPacket packet = initCDTPPackage();
    packet.setCommandSpace((short) 0x0A);
    packet.setCommand((short) 0x0F02);
    request.setUrl(baseUrl + "/" + COMMAND2);

    CDTPParams params = new CDTPParams();
    params.setHeader(headers);
    params.setQuery(queries);
    params.setBody(body);
    packet.setData(gson.toJson(params).getBytes());
    for (HttpMethod method : new HttpMethod[]{POST, PUT}) {
      request.setMethod(method);

      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(packet);

      assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
      assertThat(responseEntity.getBody()).isEqualTo(responseBody2);
    }
  }
}
