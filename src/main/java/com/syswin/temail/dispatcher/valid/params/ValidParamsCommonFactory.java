package com.syswin.temail.dispatcher.valid.params;

import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ValidParamsCommonFactory implements ValidParamsFactory {

  @Override
  public Optional<ValidParams> buildParams(CDTPPacket cdtpPacket,
      Function<CDTPPacket, String> signExtract) {
      Map<String, String> params = new HashMap<>();
      params.put(unsignedBytes, signExtract.apply(cdtpPacket));
      params.put(signature, cdtpPacket.getHeader().getSignature());
      params.put(algorithm, String.valueOf(cdtpPacket.getHeader().getSignatureAlgorithm()));
      params.put(temail, cdtpPacket.getHeader().getSender());
      return Optional.of(new ValidParams(PacketValidType.commonSignValid.getAuthUri(),params));
  }
}
