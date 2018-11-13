package com.syswin.temail.dispatcher.request;


import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin.Builder;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import com.syswin.temail.ps.common.entity.CommandType;
import java.util.HashMap;
import java.util.Map;

public class PacketMaker {

  private static final Gson gson = new Gson();

  public static CDTPPacket privateMsgPacket(String sender, String recipient, String message, String deviceId) {

    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CommandSpaceType.SINGLE_MESSAGE_CODE);
    packet.setCommand((short) 1);
    packet.setVersion(CDTP_VERSION);

    CDTPHeader header = new CDTPHeader();
    header.setSignatureAlgorithm(1);
    header.setSignature("sign");
    header.setDataEncryptionMethod(0);
    header.setTimestamp(1535713173935L);
    header.setPacketId("pkgId");
    header.setDeviceId(deviceId);
    header.setSender(sender);
    header.setReceiver(recipient);
    header.setSenderPK("SenderPK123");
    header.setReceiverPK("ReceiverPK456");
    Map<String, Object> extraData = new HashMap<>();
    extraData.put("from", sender);
    extraData.put("to", recipient);
    extraData.put("storeType", "2");
    extraData.put("type", "0");
    extraData.put("msgId", "4298F38F87DC4775B264A3753E77B443");
    header.setExtraData(gson.toJson(extraData));
    packet.setHeader(header);

    packet.setData(message.getBytes());

    return packet;
  }

  public static CDTPPacket loginPacket(String sender, String deviceId) {
    CDTPPacket packet = new CDTPPacket();
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSignatureAlgorithm(1);
    header.setTimestamp(1535713173935L);
    header.setDataEncryptionMethod(0);
    header.setPacketId("PacketId12345");
    header.setSender(sender);
    header.setSenderPK("SenderPK");
//    header.setReceiver("sean@t.email");
//    header.setReceiverPK("ReceiverPK");

    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(CommandType.LOGIN.getCode());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(header);

    Builder builder = CDTPLogin.newBuilder();

//    builder.setdevId("设备ID");
    builder.setPushToken("推送token");
    builder.setPlatform("ios/android/pc");
    builder.setOsVer("11.4");
    builder.setAppVer("1.0.0");
    builder.setLang("en、ch-zn...");
    builder.setTemail("请求发起方的temail地址");
    builder.setChl("渠道号");
    CDTPLogin cdtpLogin = builder.build();

    packet.setData(cdtpLogin.toByteArray());
    return packet;
  }
}