package com.syswin.temail.dispatcher.codec;

import com.syswin.temail.dispatcher.DispatcherProperties;
import java.lang.reflect.Array;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PacketTypeJudgeTest {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private DispatcherProperties dispatcherProperties = new DispatcherProperties();

  private PacketTypeJudge packetTypeJudge;

  @Before
  public void setUp() {
    dispatcherProperties.setOffPushType(Arrays.asList("0,17".split(",")));
    packetTypeJudge = new PacketTypeJudge(dispatcherProperties);
  }

  @Test
  public void grpMsgWillBePushed() {
    Assertions.assertThat(packetTypeJudge.isToBePushedMsg(0)).isTrue();
    Assertions.assertThat(packetTypeJudge.isToBePushedMsg(17)).isTrue();
  }

}