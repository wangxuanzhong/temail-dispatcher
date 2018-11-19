package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.Map;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Test;

import static org.apache.coyote.http11.Constants.a;
import static org.assertj.core.api.Assertions.assertThat;

public class NotificationMessageFactoryTest {

  NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactory();

  @Test
  public void extractPushOptions() throws Exception {
    CDTPHeader cdtpHeader = new CDTPHeader();
    Map<String,String> pushOptions = null;

    String nullOptions = "";
    cdtpHeader.setExtraData(nullOptions);
    pushOptions = notificationMessageFactory.extractPushOptions(cdtpHeader);
    assertThat(pushOptions).isNotNull();
    assertThat(pushOptions.size()).isEqualTo(0);

    String notFullOptions = "{\"push\":{}}";
    cdtpHeader.setExtraData(notFullOptions);
    pushOptions = notificationMessageFactory.extractPushOptions(cdtpHeader);
    assertThat(pushOptions).isNotNull();
    assertThat(pushOptions.size()).isEqualTo(0);

    String fullOptions = "{\"push\":{\"cmd\":\"0\",\"type\":\"1\"}}";
    cdtpHeader.setExtraData(fullOptions);
    pushOptions = notificationMessageFactory.extractPushOptions(cdtpHeader);
    assertThat(pushOptions).isNotNull();
    assertThat(pushOptions.get("cmd")).isEqualTo("0");
    assertThat(pushOptions.get("type")).isEqualTo("1");

    String errOptions = "{\"push\",{\"cmd\",\"0\",\"type\":\"1\"}}";
    cdtpHeader.setExtraData(errOptions);
    pushOptions = notificationMessageFactory.extractPushOptions(cdtpHeader);
    assertThat(pushOptions.size()).isEqualTo(0);
  }

}