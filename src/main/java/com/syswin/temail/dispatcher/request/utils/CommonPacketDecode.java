package com.syswin.temail.dispatcher.request.utils;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CommonPacketDecode implements PacketDecode{

  public byte[] decodeData(CDTPPacketTrans packet) {
    String data = packet.getData();
    if (data == null) {
      return new byte[0];
    }
    if (isSendSingleMsg(packet)) {
      return Base64.getUrlDecoder().decode(data);
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  public boolean isSendSingleMsg(CDTPPacketTrans packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    return commandSpace == 1 && command == 1;
  }

}
