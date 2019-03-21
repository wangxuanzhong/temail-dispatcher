package com.syswin.temail.dispatcher;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

//@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT,
    properties = {
        "app.dispatcher.cmd-map.10001.method=POST",
        "app.dispatcher.valid-strategy.crossSingleSignValid=0001-*",
        "app.dispatcher.valid-strategy.crossGroupsignValid=0002-*",
        "app.dispatcher.valid-strategy.crossTopicSignValid=000E-*",
        "app.dispatcher.valid-strategy.skipSignValid",
        "app.dispatcher.valid-strategy.commonSignValid=*-*"
})
@ActiveProfiles("dev")
public class DispatcherApplicationTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  private static final String ackMessage = "Sent ackMessage";
  private static final Gson gson = new GsonBuilder().serializeNulls().create();
  private static final String sender = "jack@t.email";
  private static final String receiver = "sean@t.email";

  private static final PacketEncoder encoder = new PacketEncoder();
  private static final CDTPPacket cdtpPacket = PacketMaker.privateMsgPacket(sender, receiver, ackMessage, "deviceId");

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeClass
  public static void setUp() {
    System.setProperty("app.dispatcher.auth-verify-url", "http://localhost:" + wireMockRule.port() + "/verify");
    System.setProperty("app.dispatcher.auth-base-url", "http://localhost:" + wireMockRule.port() + "");
    System.setProperty("app.dispatcher.cmd-map.10001.url", "http://localhost:" + wireMockRule.port() + "/usermail");

    Map<String, Object> msgPayload = new HashMap<>(gson
        .fromJson(cdtpPacket.getHeader().getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType()));

    msgPayload.put("meta", cdtpPacket.getHeader());
    msgPayload.put("msgData", Base64.getUrlEncoder().encodeToString(encoder.encode(cdtpPacket)));

    stubFor(post(urlEqualTo("/verifyRecieverTemail"))
        .withHeader(CONTENT_TYPE, equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok())))
    );

    stubFor(post(urlEqualTo("/cross/verify/single"))
        .withHeader(CONTENT_TYPE, equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok())))
    );

    stubFor(post(urlEqualTo("/usermail"))
        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_UTF8_VALUE))
        .withHeader("X-PACKET-ID", equalTo(cdtpPacket.getHeader().getPacketId()))
        .withRequestBody(equalToJson(gson.toJson(msgPayload)))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok(ackMessage)))));

  }

  @Test
  public void shouldDispatchFullPacket() {
    byte[] bytes = encoder.encode(cdtpPacket);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_OCTET_STREAM);

    HttpEntity<byte[]> httpEntity = new HttpEntity<>(bytes, headers);
    ResponseEntity<Response> responseEntity = restTemplate.postForEntity("/dispatch", httpEntity, Response.class);
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody().getData()).isEqualTo(ackMessage);
  }
}
