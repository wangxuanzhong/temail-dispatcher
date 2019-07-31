package com.syswin.temail.dispatcher.codec;

import com.syswin.temail.dispatcher.DispatcherProperties;
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

  @Mock
  private DispatcherProperties dispatcherProperties;

  private PacketTypeJudge packetTypeJudge;

  @Before
  public void setUp(){
    packetTypeJudge = new PacketTypeJudge(dispatcherProperties);
  }

  @Test
  public void grpMsgWillBePushed(){
    Assertions.assertThat(packetTypeJudge.isToBePushedMsg(100)).isTrue();
  }

}