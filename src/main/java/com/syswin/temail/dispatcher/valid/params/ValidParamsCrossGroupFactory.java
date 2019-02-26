package com.syswin.temail.dispatcher.valid.params;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import java.util.function.Function;

public class ValidParamsCrossGroupFactory implements ValidParamsFactory {

  @Override
  public Optional<ValidParams> buildParams(CDTPPacket cdtpPacket,
      Function<CDTPPacket, String> signExtract) {
    return Optional.empty();
  }
}
