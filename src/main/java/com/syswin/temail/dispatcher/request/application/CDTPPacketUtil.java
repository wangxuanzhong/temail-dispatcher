package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;

/**
 * CDTP Packet的Data数据编码解码器
 *
 * @author 姚华成
 * @date 2018-11-05
 */
public interface CDTPPacketUtil {

  String encodeData(CDTPPacket packet);

  byte[] decodeData(CDTPPacketTrans packet);

  CDTPParams buildParams(CDTPPacketTrans packet);

  default CDTPPacketTrans toTrans(CDTPPacket packet) {
    if (packet == null) {
      return null;
    }
    return new CDTPPacketTrans(packet.getCommandSpace(), packet.getCommand(), packet.getVersion(),
        packet.getHeader().clone(), encodeData(packet));
  }

  default CDTPPacket fromTrans(CDTPPacketTrans packetTrans) {
    if (packetTrans == null) {
      return null;
    }
    return new CDTPPacket(packetTrans.getCommandSpace(), packetTrans.getCommand(), packetTrans.getVersion(),
        packetTrans.getHeader().clone(),
        decodeData(packetTrans));
  }
}
