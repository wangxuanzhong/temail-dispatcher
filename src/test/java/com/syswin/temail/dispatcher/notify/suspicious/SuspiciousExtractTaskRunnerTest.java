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

import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.notify.NotificationMessageFactory;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SuspiciousExtractTaskRunnerTest {

  private final NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactory();
  private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
  private SuspiciousExtractTaskRunner suspiciousExtractTaskRunner;
  private final Random random = new Random(27);
  private final DispatcherProperties dispatcherProperties = Mockito
      .mock(DispatcherProperties.class);
  private final Gson gson = new Gson();

  @Before
  public void init() {
    when(this.dispatcherProperties.getRelationBaseUrl())
        .thenReturn("http://temail-relation.service.innertools.com:8081");
  }

  @Test
  public void offerAndTakeWorkWell() {
    int totalPuts = 1000;
    List<CDTPHeader> cdtpHeaderList = new ArrayList<>();
    CDTPHeader cdtpHeader = new CDTPHeader();
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    buildRunner();

    executorService.submit(() -> {
      for (int i = 0; i < totalPuts; i++) {
        try {
          suspiciousExtractTaskRunner.offer(cdtpHeader);
          TimeUnit.MILLISECONDS.sleep(random.nextInt(5));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    executorService.submit(() -> {
      for (int i = 0; i < totalPuts; i++) {
        try {
          cdtpHeaderList.add(suspiciousExtractTaskRunner.take());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    Awaitility.waitAtMost(5, TimeUnit.SECONDS).until(() -> {
      return totalPuts == cdtpHeaderList.size();
    });
  }

  @Test
  public void testExtract() {
    CDTPHeader cdtpHeader = buildCdtpHeader();
    buildRunner();

    Optional<RelationBind> relationBind = this.suspiciousExtractTaskRunner.extract(cdtpHeader);
    Assertions.assertThat(relationBind.isPresent()).isTrue();
    Assertions.assertThat(relationBind.get().getReceiver()).isEqualTo("zhangweiliang@syswin.com");
    Assertions.assertThat(relationBind.get().getSender()).isEqualTo("es_alert@syswin.com");
    Assertions.assertThat(relationBind.get().getContactType())
        .isEqualTo(RelationType.suspicious.getCode());

    relationBind = this.suspiciousExtractTaskRunner.extract(new CDTPHeader());
    Assertions.assertThat(relationBind.isPresent()).isFalse();
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

  @Test
  public void testHandleTask() {
    buildRunner();

    setRestTemplate(true, null);
    Assertions.assertThat(this.suspiciousExtractTaskRunner.handleTask(this.buildCdtpHeader()))
        .isTrue();

    setRestTemplate(false, "test-fail");
    Assertions.assertThat(this.suspiciousExtractTaskRunner.handleTask(this.buildCdtpHeader()))
        .isFalse();

  }

  private void setRestTemplate(boolean result, String msg) {
    if (result) {
      Mockito
          .when(
              restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(
                  HttpEntity.class), Mockito.any(ParameterizedTypeReference.class))).thenReturn(
          ResponseEntity.ok(Response.ok(ImmutableMap.of("result", "success"))));

    } else {
      Mockito
          .when(
              restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(
                  HttpEntity.class), Mockito.any(ParameterizedTypeReference.class))).thenReturn(
          new ResponseEntity(Response.failed(HttpStatus.BAD_REQUEST, msg), HttpStatus.BAD_REQUEST));
    }
  }


  @Test
  public void testRun() throws Exception {
    buildRunner();
    setRestTemplate(true, null);
    this.suspiciousExtractTaskRunner.run(null);
    for (int i = 0; i < 1000; i++) {
      this.suspiciousExtractTaskRunner.offer(this.buildCdtpHeader());
    }
    Awaitility.waitAtMost(10, TimeUnit.SECONDS).until(()->{
      return suspiciousExtractTaskRunner.isEmpty();
    });
  }


  private void buildRunner() {
    this.suspiciousExtractTaskRunner = new SuspiciousExtractTaskRunner(
        restTemplate, notificationMessageFactory, dispatcherProperties, t -> {
    });
  }


}