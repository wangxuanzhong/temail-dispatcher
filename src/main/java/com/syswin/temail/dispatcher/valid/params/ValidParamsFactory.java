package com.syswin.temail.dispatcher.valid.params;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import java.util.function.Function;

public interface ValidParamsFactory {

  String unsignedBytes = "UNSIGNED_BYTES";
  String signature = "SIGNATURE";
  String algorithm = "algorithm";
  String senderTemail = "SenderTeMail";
  String receiverTemail = "RecieverTeMail";
  String senderPublicKey = "SenderPublicKey";
  String temail="TeMail";

  Optional<ValidParams> buildParams(CDTPPacket cdtpPacket,
      Function<CDTPPacket, String> signExtract);

}
