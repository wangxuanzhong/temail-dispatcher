package com.syswin.temail.dispatcher.request.utils;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;

public interface PacketDecode {

  public byte[] decodeData(CDTPPacketTrans packet) ;

  public boolean isSendSingleMsg(CDTPPacketTrans packet);

}
