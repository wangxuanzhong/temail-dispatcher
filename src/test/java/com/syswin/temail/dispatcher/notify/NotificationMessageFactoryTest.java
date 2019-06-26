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