package com.syswin.temail.dispatcher.valid.params;

import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ValidParamsCrossSingleFactory implements ValidParamsFactory {

  @Override
  public Optional<ValidParams> buildParams(CDTPPacket cdtpPacket,
      Function<CDTPPacket, String> signExtract) {
    Map<String, String> params = new HashMap<>();
    params.put(unsignedBytes, signExtract.apply(cdtpPacket));
    params.put(signature, cdtpPacket.getHeader().getSignature());
    params.put(algorithm, String.valueOf(cdtpPacket.getHeader().getSignatureAlgorithm()));
    params.put(senderTemail, cdtpPacket.getHeader().getSender());
    params.put(receiverTemail, cdtpPacket.getHeader().getReceiver());
    params.put(senderPublicKey, cdtpPacket.getHeader().getSenderPK());
    return Optional.of(new ValidParams(PacketValidType.crossSingleSignValid.getAuthUri(),params));
  }
}
