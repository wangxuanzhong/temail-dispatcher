package com.syswin.temail.dispatcher.request.application;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.nio.charset.StandardCharsets;

/**
 * @author 姚华成
 * @date 2018-11-05
 */
public class SimplePacketUtil implements CDTPPacketUtil {

  private static Gson gson = new Gson();

  @Override
  public String encodeData(CDTPPacket packet) {
    byte[] data;
    if (packet == null || (data = packet.getData()) == null) {
      return "";
    }
    return new String(data, StandardCharsets.UTF_8);
  }

  @Override
  public byte[] decodeData(CDTPPacketTrans packet) {
    String data;
    if (packet == null || (data = packet.getData()) == null) {
      return new byte[0];
    }
    return data.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public CDTPParams buildParams(CDTPPacketTrans packet) {
    return gson.fromJson(packet.getData(), CDTPParams.class);
  }
}
