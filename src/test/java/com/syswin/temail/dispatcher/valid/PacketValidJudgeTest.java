package com.syswin.temail.dispatcher.valid;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.dispatcher.valid.params.ValidParams;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class PacketValidJudgeTest {

  private CDTPPacket cdtpPacket;

  private final DispatcherProperties dispatcherProperties = new DispatcherProperties();

  private final PacketValidJudge packetValidJudge = new PacketValidJudge(dispatcherProperties);

  @Before
  public void init() {
    cdtpPacket = PacketMaker.privateMsgPacket("jack@t.email",
        "sean@t.email", "Sent ackMessage", "deviceId");

    dispatcherProperties.setValidStrategy(ImmutableMap.of(
        PacketValidType.commonSignValid.getMapKeycode(), ImmutableList.of("*-*"),
        PacketValidType.crossSingleSignValid.getMapKeycode(), ImmutableList.of("0001-*"),
        PacketValidType.crossTopicSignValid.getMapKeycode(), ImmutableList.of("000E-*"),
        PacketValidType.crossGroupsignValid.getMapKeycode(), ImmutableList.of("0002-*"),
        PacketValidType.skipSignValid.getMapKeycode(), ImmutableList.of("*-*","00EE-0001")));
  }


  @Test
  public void singleType(){
    cdtpPacket.setCommandSpace((short) 1);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isEqualTo(PacketValidType.crossSingleSignValid.getAuthUri());
    Assertions.assertThat(validParams.get().getParams()).isNotEmpty();

  }

  @Test
  public void groupType(){
    cdtpPacket.setCommandSpace((short) 2);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams.isPresent()).isFalse();

  }

  @Test
  public void topicType(){
    cdtpPacket.setCommandSpace((short) 0x000E);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isEqualTo(PacketValidType.crossTopicSignValid.getAuthUri());
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(5);
  }

  @Test
  public void commonType(){
    cdtpPacket.setCommandSpace((short) 0x0016);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isEqualTo(PacketValidType.commonSignValid.getAuthUri());
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(4);
  }


  @Test
  public void skipType(){
    cdtpPacket.setCommandSpace((short) 0x00EE);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams.isPresent()).isFalse();
  }



}