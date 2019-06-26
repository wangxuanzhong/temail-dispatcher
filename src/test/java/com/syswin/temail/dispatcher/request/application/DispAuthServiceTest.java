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

package com.syswin.temail.dispatcher.request.application;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.dispatcher.valid.PacketValidJudge;
import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

public class DispAuthServiceTest {

  private final DispatcherProperties dispatcherProperties = new DispatcherProperties();

  private final PacketTypeJudge packetTypeJudge = new PacketTypeJudge(dispatcherProperties);

  private final CommandAwarePacketUtil commandAwarePacketUtil = new CommandAwarePacketUtil(packetTypeJudge);

  private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

  private final DispAuthService dispAuthService = new DispAuthService(restTemplate,
      dispatcherProperties, commandAwarePacketUtil, new PacketValidJudge(dispatcherProperties));

  private static final CDTPPacket cdtpPacket = PacketMaker
      .privateMsgPacket("jack@t.email", "sean@t.email", "Sent ackMessage", "deviceId");

  @Before
  public void initProperties() {
    dispatcherProperties.setValidStrategy(ImmutableMap.of(
        PacketValidType.commonSignValid.getMapKeycode(), ImmutableList.of("*-*"),
        PacketValidType.crossSingleSignValid.getMapKeycode(), ImmutableList.of("0001-*"),
        PacketValidType.crossTopicSignValid.getMapKeycode(), ImmutableList.of("000E-*"),
        PacketValidType.crossGroupsignValid.getMapKeycode(), ImmutableList.of("0002-*"),
        PacketValidType.skipSignValid.getMapKeycode(), ImmutableList.of("EEEE-*")
    ));
  }

  @Test
  public void statusAlwaysOkIfGroupType() {
    cdtpPacket.setCommandSpace((short) 2);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Assertions.assertThat(dispAuthService.verify(cdtpPacket)
        .getStatusCode().is2xxSuccessful()).isTrue();
  }

}