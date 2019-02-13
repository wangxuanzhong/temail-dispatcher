package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

public class AuthServiceTest {

  private final PacketTypeJudge packetTypeJudge = new PacketTypeJudge();

  private final CommandAwarePacketUtil commandAwarePacketUtil = new CommandAwarePacketUtil(packetTypeJudge);

  private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

  private final AuthService authService = new AuthService(restTemplate,
      "http://auth.innermail.com:8081/verify", commandAwarePacketUtil);

  @Test
  public void statusAlwaysOkIfGroupType(){
    CDTPPacket cdtpPacket = new CDTPPacket();
    cdtpPacket.setCommandSpace((short) 2);
    Assertions.assertThat(authService.verify(cdtpPacket).getStatusCode().is2xxSuccessful()).isTrue();
  }

}