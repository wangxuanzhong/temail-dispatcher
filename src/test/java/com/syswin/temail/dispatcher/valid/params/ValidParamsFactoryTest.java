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