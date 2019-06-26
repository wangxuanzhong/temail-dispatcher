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

package com.syswin.temail.dispatcher.notify.suspicious;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
//    "app.dispatcher.relation-base-url=http://172.31.243.83:8081"
})
public class SuspiciousExtractTaskRunnerInteTest {

  private static final Gson gson = new Gson();

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(
      WireMockConfiguration.wireMockConfig().dynamicPort());

  @BeforeClass
  public static void setUp() {
    System.setProperty("app.dispatcher.relation-base-url",
        "http://localhost:" + WIRE_MOCK_RULE.port());

    stubFor(post(urlEqualTo("/relation/suspicious"))
        .withHeader(CONTENT_TYPE, equalTo("application/json;charset=UTF-8"))
        .willReturn(
            aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value()).withBody(gson.toJson(Response.ok(ImmutableMap.of(
                "sender", "string",
                "receiver", "string",
                "contactType", 0
            )))))
    );
  }

  @Autowired
  private TaskExecutor suspiciousExtractTaskRunner;

  @Test
  public void testHandleTask() {
    boolean handleTask = this.suspiciousExtractTaskRunner.handleTask(this.buildCdtpHeader());
    Awaitility.waitAtMost(2, TimeUnit.SECONDS).until(() -> {
      return handleTask;
    });
  }


  @NotNull
  private CDTPHeader buildCdtpHeader() {
    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setPacketId("5121b5e8-4260-4f74-9e41-c6e3f232845f");
    cdtpHeader.setReceiver("zhangweiliang@syswin.com");
    cdtpHeader.setReceiverPK("MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBmJHHgykcnuDDL0FPbZjDci");
    cdtpHeader.setSender("mail_gateway@msgseal.com");
    cdtpHeader.setSenderPK("MIGbMBAGByqGSM49AgEGBSuBBAAjA4G");
    cdtpHeader.setSignatureAlgorithm(2);
    cdtpHeader.setTimestamp(1557887319329L);

    cdtpHeader.setExtraData("{\n"
        + "         \"from\":\"es_alert@syswin.com\",\n"
        + "         \"msgId\":\"d0fe0dee-1dcb-49ab-bbef-a9aa2734591a\",\n"
        + "         \"storeType\":1,\"to\":\"zhangweiliang@syswin.com\",\n"
        + "         \"type\":0, \n"
        + "         \"suspicious\":1"
        + "    }");
    return cdtpHeader;
  }


}