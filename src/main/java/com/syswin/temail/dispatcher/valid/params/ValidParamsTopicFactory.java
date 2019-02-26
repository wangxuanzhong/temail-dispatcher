package com.syswin.temail.dispatcher.valid.params;

import com.google.common.collect.ImmutableMap;
import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import java.util.function.Function;

public class ValidParamsTopicFactory implements ValidParamsFactory {

  @Override
  public Optional<ValidParams> buildParams(CDTPPacket cdtpPacket,
      Function<CDTPPacket, String> signExtract) {
    return Optional.of(new ValidParams(PacketValidType.crossTopicSignValid.getAuthUri(),
        ImmutableMap.of(receiverTemail, cdtpPacket.getHeader().getReceiver())));
  }
}
