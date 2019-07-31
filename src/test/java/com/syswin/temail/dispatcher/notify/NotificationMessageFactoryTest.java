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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.reflections.scanners.TypeElementsScanner;

@Slf4j
public class NotificationMessageFactoryTest {

  final Gson gson = new Gson();

  final NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactory();

  final CDTPHeader cdtpHeader = new CDTPHeader();

  @Test
  public void nullIfExtraDataEmpty() {
    Map<String, String> pushOptions = extractMap("");
    assertThat(pushOptions.get("cmd")).isNull();
    assertThat(pushOptions.get("type")).isNull();
  }

  @Test
  public void nullIfPushEmpty() {
    Map<String, String> pushOptions = extractMap("{\"push\":{}}");
    assertThat(pushOptions.get("cmd")).isNull();
    assertThat(pushOptions.get("type")).isNull();
  }

  @Test
  public void notNullIfPushFull() {
    Map<String, String> pushOptions = extractMap("{\"push\":{\"cmd\":\"0\",\"type\":\"1\"}}");
    assertThat(pushOptions.get("cmd")).isNotNull();
    assertThat(pushOptions.get("type")).isNotNull();
  }

  @Test
  public void notNullIfPushError() {
    Map<String, String> pushOptions = extractMap("{\"push\",{\"cmd\",\"0\",\"type\":\"1\"}}");
    assertThat(pushOptions.get("cmd")).isNull();
    assertThat(pushOptions.get("type")).isNull();
  }


  @Test
  public void notNullIfExtraDataFull() {
    Map<String, String> pushOptions = extractMap(
        "{\"from\":\"lhd-111111888@systoontest.com\",\"msgId\":\"c125e03f-3f23-4b41-8ba1-a95d11d315b1\","
            + "\"push\":{\"cmd\":\"singleVideoAudio\",\"type\":\"1\"},"
            + "\"storeType\":1,\"to\":\"wuning@systoontest.com\",\"type\":0}");
    assertThat(pushOptions.get("cmd")).isNotNull();
    assertThat(pushOptions.get("type")).isNotNull();
  }

  private Map<String, String> extractMap(String options) {
    Map<String, String> pushOptions;
    cdtpHeader.setExtraData(options);
    pushOptions = notificationMessageFactory.extractPushOptions(cdtpHeader);
    return pushOptions;
  }

  @Test
  public void willExtratGroupTemailIfGroupMsg() {
    String msg = "{\"receiver\":\"cdtp_test2@msgseal.com\",\"eventType\":0,\"header\":\"{\\\"deviceId\\\":\\\"327b805a-7258-4081-93cb-e39f10445bed\\\",\\\"signatureAlgorithm"
        + "\\\":2,\\\"signature\\\":\\\"MIGHAkEzTwHSPU89RViveRLR7h3ISjsPchiUJWc0AueHV6xsMtgglOfnIRE0BXRLv8e3B0Hgc_qDahiqUaG61iAKi6zGFwJCAOa_z1e4uADRBcfKiv0Jzk_HtEPfYO5TbJR6A"
        + "o-LthcqcUeH_0MrqExwk_3fNXxWnew2qHB3Martu0m8tTOPH-uz\\\",\\\"dataEncryptionMethod\\\":4,\\\"timestamp\\\":1564563090249,\\\"packetId\\\":\\\"40224a82-2df8-4e9f-8e8"
        + "a-2a83c70ef976:cdtp_test2@msgseal.com:1\\\",\\\"sender\\\":\\\"d.213675337@testly.ly\\\",\\\"senderPK\\\":\\\"MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAJWlMskUP7BYADi9f"
        + "o8LiWL9S_0va6anP_s5uVwsqGymwfowgfwGU9cI5wWxzL_Xby4WM22M_DdVbVaPw3xIzwpYBlhBnaMVubBjB-A1NIH9r2Mzh-pSeR1qiA11wELCogN215Kj9CziViApxmJe0SB-h3zEekVdZyvSeh1VVEeHefF0\\\""
        + ",\\\"receiver\\\":\\\"cdtp_test2@msgseal.com\\\",\\\"receiverPK\\\":\\\"MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAxr11jt7ZRJLyGxpeA90AXlgK6_BQ6TJwQxJXPTXqCE63388VUJ8qQEq"
        + "eOTA17LmCyM_KGkfiYqN7jQ7R5ZLrqHAAhBGOa8REy_7rBj3yPvUTTrVUUEC4_BXRt-hv2QsrB0_XgKvdWdPBxpvVXdJFgzP25um1Rfk9KiU8SZXAVDTgYmc\\\",\\\"extraData\\\":\\\"{\\\\\\\"storeTyp"
        + "e\\\\\\\":1,\\\\\\\"attachmentSize\\\\\\\":0,\\\\\\\"sessionExtData\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"author\\\\\\\":\\\\\\\"stest098484852657@msgseal.com\\\\\\\","
        + "\\\\\\\"msgId\\\\\\\":\\\\\\\"d2270224-7b19-418a-a34d-3bd8aff977d6\\\\\\\",\\\\\\\"from\\\\\\\":\\\\\\\"d.213675337@testly.ly\\\\\\\",\\\\\\\"to\\\\\\\":\\\\\\\"cd"
        + "tp_test2@msgseal.com\\\\\\\",\\\\\\\"type\\\\\\\":0,\\\\\\\"timestamp\\\\\\\":1564563090212}\\\",\\\"targetAddress\\\":\\\"msgseal.t.email:8099\\\"}\",\"data\":\""
        + "{\\\"xPacketId\\\":\\\"40224a82-2df8-4e9f-8e8a-2a83c70ef976:cdtp_test2@msgseal.com:1\\\",\\\"eventSeqId\\\":148488,\\\"eventType\\\":0,\\\"msgId\\\":\\\"d2270224-7"
        + "b19-418a-a34d-3bd8aff977d6\\\",\\\"seqId\\\":88462,\\\"message\\\":\\\"AAAFWwABAAEAAQQPCiQzMjdiODA1YS03MjU4LTQwODEtOTNjYi1lMzlmMTA0NDViZWQQAhq4AU1JR0hBa0V6VHdIU1BV"
        + "ODlSVml2ZVJMUjdoM0lTanNQY2hpVUpXYzBBdWVIVjZ4c010Z2dsT2ZuSVJFMEJYUkx2OGUzQjBIZ2NfcURhaGlxVWFHNjFpQUtpNnpHRndKQ0FPYV96MWU0dUFEUkJjZktpdjBKemtfSHRFUGZZTzVUYkpSNkFvLUx0"
        + "aGNxY1VlSF8wTXJxRXh3a18zZk5YeFduZXcycUhCM01hcnR1MG04dFRPUEgtdXogBClJKzpHbAEAADI9NDAyMjRhODItMmRmOC00ZTlmLThlOGEtMmE4M2M3MGVmOTc2OmNkdHBfdGVzdDJAbXNnc2VhbC5jb206MToV"
        + "ZC4yMTM2NzUzMzdAdGVzdGx5Lmx5QtMBTUlHYk1CQUdCeXFHU000OUFnRUdCU3VCQkFBakE0R0dBQVFBSldsTXNrVVA3QllBRGk5Zm84TGlXTDlTXzB2YTZhblBfczV1VndzcUd5bXdmb3dnZndHVTljSTV3V3h6TF9Y"
        + "Ynk0V00yMk1fRGRWYlZhUHczeEl6d3BZQmxoQm5hTVZ1YkJqQi1BMU5JSDlyMk16aC1wU2VSMXFpQTExd0VMQ29nTjIxNUtqOUN6aVZpQXB4bUplMFNCLWgzekVla1ZkWnl2U2VoMVZWRWVIZWZGMEoWY2R0cF90ZXN0"
        + "MkBtc2dzZWFsLmNvbVLTAU1JR2JNQkFHQnlxR1NNNDlBZ0VHQlN1QkJBQWpBNEdHQUFRQXhyMTFqdDdaUkpMeUd4cGVBOTBBWGxnSzZfQlE2VEp3UXhKWFBUWHFDRTYzMzg4VlVKOHFRRXFlT1RBMTdMbUN5TV9LR2tm"
        + "aVlxTjdqUTdSNVpMcnFIQUFoQkdPYThSRXlfN3JCajN5UHZVVFRyVlVVRUM0X0JYUnQtaHYyUXNyQjBfWGdLdmRXZFBCeHB2VlhkSkZnelAyNXVtMVJmazlLaVU4U1pYQVZEVGdZbWNq7gF7InN0b3JlVHlwZSI6MSwiY"
        + "XR0YWNobWVudFNpemUiOjAsInNlc3Npb25FeHREYXRhIjoiIiwiYXV0aG9yIjoic3Rlc3QwOTg0ODQ4NTI2NTdAbXNnc2VhbC5jb20iLCJtc2dJZCI6ImQyMjcwMjI0LTdiMTktNDE4YS1hMzRkLTNiZDhhZmY5NzdkNi"
        + "IsImZyb20iOiJkLjIxMzY3NTMzN0B0ZXN0bHkubHkiLCJ0byI6ImNkdHBfdGVzdDJAbXNnc2VhbC5jb20iLCJ0eXBlIjowLCJ0aW1lc3RhbXAiOjE1NjQ1NjMwOTAyMTJ9chRtc2dzZWFsLnQuZW1haWw6ODA5OUFBQUF"
        + "Rd0FBQUVBQUFBQmdBQUFBVWdNQUpvSzI1cFVHWWJzMG1oYTd4TGUzMDZOajIzV1ZFZ2xZckdtNUtreFY0SkprS2sxY3Ezb0IxSUtLb2ZFakw1WGdnNHJaa3VaLVhNdk5MQ19iOFNTOC1nUlBQWHhxT0dvVTRsb2lQOVNm"
        + "YXJsc2FjNjdybEVaY01QcTNkXy1ub3BnTTFfeEt4RnU2SXpIM1JOczU1RERLUldLQm4zTDFEVGVnT2F3d2s4TjVqQ1Z3R3p4eXRXSlh6WU5lRXI5bDRmVG90TU1LNmkwMDVNNUZYM2ZmX0g0WkM3Ym5BNUZYNEIwVkRaYV"
        + "VJellDX0JWYzBfdGw4OE4xcnBoeWttZEFJcW5DcE92Q2pUdDVjbG9wakxoS3N2c1VkdllxSWgtNjRqeUFOX3hMZnA1czhveA\\\\u003d\\\\u003d\\\",\\\"from\\\":\\\"d.213675337@testly.ly\\\",\\\""
        + "to\\\":\\\"cdtp_test2@msgseal.com\\\",\\\"timestamp\\\":1564563090371,\\\"owner\\\":\\\"cdtp_test2@msgseal.com\\\",\\\"author\\\":\\\"stest098484852657@msgseal.com\\\""
        + ",\\\"sessionExtData\\\":\\\"\\\"}\"}"         ;

    MessageBody messageBody = gson.fromJson(msg, MessageBody.class);
    log.info("receive msg: {}", messageBody.toString());
    CDTPHeader header = gson.fromJson(messageBody.getHeader(), CDTPHeader.class);
    Optional<String> pushMessage = notificationMessageFactory.getPushMessage(messageBody.getReceiver(), header, messageBody.getData());
    Assertions.assertThat(pushMessage.isPresent()).isTrue();
    Assertions.assertThat(gson.fromJson(pushMessage.get(), Map.class).get("from")).isNotNull();

    PushData pushData = gson.fromJson(messageBody.getData(), PushData.class);
    pushData.setEventType(100);
    pushData.setGroupTemail("newSetGroupTemail@syswin.com");
    messageBody.setData(gson.toJson(pushData));
    pushMessage = notificationMessageFactory.getPushMessage(messageBody.getReceiver(), header, messageBody.getData());
    Assertions.assertThat(gson.fromJson(pushMessage.get(), Map.class).get("from")).isEqualTo("newSetGroupTemail@syswin.com");
  }


}