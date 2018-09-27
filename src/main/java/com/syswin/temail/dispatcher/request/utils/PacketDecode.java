package com.syswin.temail.dispatcher.request.utils;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;

public interface PacketDecode {

  byte[] decodeData(CDTPPacketTrans packet);

  boolean isSendSingleMsg(CDTPPacketTrans packet);

}
