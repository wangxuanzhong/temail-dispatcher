package com.syswin.temail.dispatcher.valid.params;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import java.util.function.Function;

public interface ValidParamsFactory {

  final String unsignedBytes = "UNSIGNED_BYTES";
  final String signature = "SIGNATURE";
  final String algorithm = "algorithm";
  final String senderTemail = "SenderTeMail";
  final String receiverTemail = "RecieverTeMail";
  final String senderPublicKey = "SenderPublicKey";
  final String temail="TeMail";

  Optional<ValidParams> buildParams(CDTPPacket cdtpPacket,
      Function<CDTPPacket, String> signExtract);

}
