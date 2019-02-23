package com.syswin.temail.dispatcher.codec;

import com.google.common.collect.ImmutableList;
import com.syswin.temail.dispatcher.DispatcherProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class PacketTypeJudgeTest {

  private PacketTypeJudge packetTypeJudge = new PacketTypeJudge(null);

  @Before
  public void init(){
    DispatcherProperties dispatcherProperties = new DispatcherProperties();
    Map map = new HashMap();
    map.put(packetTypeJudge.getReceiverVlalidKey(), ImmutableList.of("0001-0001", "0001-0005", "0001-0006", "0001-1005", "0001-1006", "0001-100B"));
    map.put(packetTypeJudge.getSkipedKey(), ImmutableList.of("000E-0001", "000E-0002", "000E-0004"));
    dispatcherProperties.setValidStrategy(map);
    packetTypeJudge.setDispatcherProperties(dispatcherProperties);
  }


  @Test
  public void test(){
    Assertions.assertThat(packetTypeJudge.isVerifyRecieverType((short)1, (short)0x1)).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifyRecieverType((short)1, (short)0x5)).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifyRecieverType((short)1, (short)0x6)).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifyRecieverType((short)1, (short)0x1005)).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifyRecieverType((short)1, (short)(0x1006))).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifyRecieverType((short)1, (short)(0x100B))).isTrue();

    Assertions.assertThat(packetTypeJudge.isVerifySkipped((short)(0xE), (short)0x1)).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifySkipped((short)(0xE), (short)0x2)).isTrue();
    Assertions.assertThat(packetTypeJudge.isVerifySkipped((short)(0xE), (short)(0x4))).isTrue();

  }

}