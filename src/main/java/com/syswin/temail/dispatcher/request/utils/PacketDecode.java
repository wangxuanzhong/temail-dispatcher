package com.syswin.temail.dispatcher.request.utils;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;

public interface PacketDecode {

  boolean isSendSingleMsg(CDTPPacketTrans packet);

  boolean isGroupJoin(CDTPPacketTrans packet);

  byte[] decodeData(CDTPPacketTrans packet);

}
