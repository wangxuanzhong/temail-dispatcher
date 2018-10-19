package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PacketDecode {

  public static boolean isSendSingleMsg(CDTPPacketTrans packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    return commandSpace == 1 && command == 1;
  }

  public static boolean isGroupJoin(CDTPPacketTrans packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    return commandSpace == 2 && command == 0x0107;
  }

  public static byte[] decodeData(CDTPPacketTrans packet) {
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
}
