package com.syswin.temail.dispatcher.notify;

import static org.assertj.core.api.Assertions.assertThat;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.Map;
import org.junit.Test;

public class NotificationMessageFactoryTest {

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

}