package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.utils.PacketUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DispatcherPacketUtil {

  public static boolean isSendSingleMsg(CDTPPacketTrans packet) {
    return packet.getCommandSpace() == 1 && packet.getCommand() == 1;
  }

  public static boolean isSendGroupMsg(CDTPPacketTrans packet) {
    return packet.getCommandSpace() == 2 && packet.getCommand() == 1;
  }

  public static boolean isGroupJoin(CDTPPacketTrans packet) {
    return packet.getCommandSpace() == 2 && packet.getCommand() == 0x0107;
  }

  public static byte[] decodeData(CDTPPacketTrans packet) {
    String data = packet.getData();
    if (data == null) {
      return new byte[0];
    }
    if (isSendSingleMsg(packet)) {
      byte[] packetBytes = Base64.getUrlDecoder().decode(data);
      CDTPPacket originalPacket = PacketUtil.unpacket(packetBytes);
      return originalPacket.getData();
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }
}
