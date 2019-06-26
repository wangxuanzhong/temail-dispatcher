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

package com.syswin.temail.dispatcher.valid.params;

import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class ValidParamsFactoryTest {

  private CDTPPacket cdtpPacket;

  @Before
  public void init() {
    cdtpPacket = PacketMaker.privateMsgPacket("jack@t.email",
        "sean@t.email", "Sent ackMessage", "deviceId");
    cdtpPacket.setCommandSpace((short) 2);
    cdtpPacket.setCommand((short) 1);
  }

  @Test
  public void testCommon() {
    Optional<ValidParams> validParams = new ValidParamsCommonFactory()
        .buildParams(cdtpPacket, t -> t.getHeader().getSender());
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isNotNull();
    Assertions.assertThat(validParams.get().getParams()).isNotEmpty();
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(4);
  }

  @Test
  public void testGroup() {
    Optional<ValidParams> validParams = new ValidParamsCrossGroupFactory()
        .buildParams(cdtpPacket, t -> t.getHeader().getSender());
    Assertions.assertThat(validParams.isPresent()).isFalse();
  }

  @Test
  public void testSingle() {
    Optional<ValidParams> validParams = new ValidParamsCrossSingleFactory()
        .buildParams(cdtpPacket, t -> t.getHeader().getSender());
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isNotNull();
    Assertions.assertThat(validParams.get().getParams()).isNotEmpty();
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(6);
  }

  @Test
  public void testTopic() {
    Optional<ValidParams> validParams = new ValidParamsTopicFactory()
        .buildParams(cdtpPacket, t -> t.getHeader().getSender());
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isNotNull();
    Assertions.assertThat(validParams.get().getParams()).isNotEmpty();
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(5);
  }


  @Test
  public void testSkip() {
    Optional<ValidParams> validParams = new ValidParamsSkipFactory()
        .buildParams(cdtpPacket, t -> t.getHeader().getSender());
    Assertions.assertThat(validParams.isPresent()).isFalse();
  }


}