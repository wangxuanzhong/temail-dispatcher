package com.syswin.temail.dispatcher.request;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans.Header;

public class PacketMaker {

  public PacketMaker() {
  }

  public static CDTPPacketTrans initCDTPPackage() {
    CDTPPacketTrans packet = new CDTPPacketTrans();
    packet.setCommand((short) 0);
    packet.setCommand((short) 1);
    packet.setVersion((short) 1);

    Header header = new Header();
    header.setSignatureAlgorithm(1);
    header.setSignature("sign");
    header.setDataEncryptionMethod(1);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId("pkgId");
    header.setSender("yaohuacheng@syswin.com");
    header.setSenderPK("SenderPK(");
    header.setReceiver("yaohuacheng@syswin.com");
    header.setReceiverPK("ReceiverPK(");
    packet.setHeader(header);

    return packet;
  }
}