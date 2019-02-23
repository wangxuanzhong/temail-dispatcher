package com.syswin.temail.dispatcher.codec;

import static org.junit.Assert.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;

@Slf4j
public class PacketTypeJudgeTest {

  private PacketTypeJudge packetTypeJudge = new PacketTypeJudge();

  @Test
  public void test(){
    Assertions.assertThat(packetTypeJudge.isToBeVerifyRecieverTemail((short)1, (short)0x1)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifyRecieverTemail((short)1, (short)0x5)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifyRecieverTemail((short)1, (short)0x6)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifyRecieverTemail((short)1, (short)0x1005)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifyRecieverTemail((short)1, (short)(0x1006))).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifyRecieverTemail((short)1, (short)(0x100B))).isTrue();

    Assertions.assertThat(packetTypeJudge.isToBeVerifySkipped((short)(0xE), (short)0x1)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifySkipped((short)(0xE), (short)0x2)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBeVerifySkipped((short)(0xE), (short)(0x4))).isTrue();

  }

}