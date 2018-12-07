package com.syswin.temail.dispatcher.request.application;

import static java.nio.charset.StandardCharsets.UTF_8;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.packet.PacketUtil;
import com.syswin.temail.ps.common.packet.SimplePacketUtil;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-11-05
 */
@Slf4j
public class CommandAwarePacketUtil extends PacketUtil {

  private static final Gson gson = new Gson();
  private final SimplePacketUtil defaultPacketUtil;
  private final PacketTypeJudge packetTypeJudge;

  public CommandAwarePacketUtil(PacketTypeJudge packetTypeJudge) {
    this(SimplePacketUtil.INSTANCE, packetTypeJudge);
  }

  public CommandAwarePacketUtil(SimplePacketUtil defaultPacketUtil, PacketTypeJudge packetTypeJudge) {
    this.defaultPacketUtil = defaultPacketUtil;
    this.packetTypeJudge = packetTypeJudge;
  }

  @Override
  protected BodyExtractor getBodyExtractor() {
    return SimpleBodyExtractor.INSTANCE;
  }

  @Override
  public String encodeData(CDTPPacket packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
      return Base64.getUrlEncoder().encodeToString(packet.getData());
    } else {
      return defaultPacketUtil.encodeData(packet);
    }
  }

  public byte[] decodeData(CDTPPacket packet) {
    byte[] data;
    if (packet == null || (data = packet.getData()) == null) {
      return new byte[0];
    }

    //decode package for verify  sign
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
      byte[] dataBytes = Base64.getUrlDecoder().decode(data);
      CDTPPacket originalPacket = unpack(dataBytes);
      data =  originalPacket.getData();
    }

    log.debug("payload before sha256 is : {}", data.toString());

    return data;
  }

  @Override
  public byte[] decodeData(CDTPPacketTrans packet) {
    throw new UnsupportedOperationException();
  }

  public CDTPParams buildParams(CDTPPacket packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    try {
      if (isSendSingleMsg(commandSpace, command)) {
        return buildSendSingleMsgParams(packet);
      } else if (isSendGroupMsg(commandSpace, command)) {
        return buildSendGroupMsgParams(packet);
      } else {
        return gson.fromJson(new String(packet.getData(), UTF_8), CDTPParams.class);
      }
    } catch (JsonSyntaxException e) {
      log.error("fail to parse json format data of Body, param：{}", packet);
      throw new DispatchException(e, packet);
    }
  }

  boolean isSendSingleMsg(short commandSpace, short command) {
    return packetTypeJudge.isPrivateDecryptType(commandSpace, command);
  }

  private boolean isSendGroupMsg(short commandSpace, short command) {
    return packetTypeJudge.isGroupDecryptType(commandSpace, command);
  }

  boolean isGroupJoin(short commandSpace, short command) {
    return packetTypeJudge.isGroupJoin(commandSpace, command);
  }

  private CDTPParams buildSendSingleMsgParams(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    Map<String, Object> extraData = gson
        .fromJson(header.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());
    Map<String, Object> body = new HashMap<>(extraData);
    body.put("msgData", encode(packet.getData()));
    body.put("meta", header);

    return new CDTPParams(body);
  }

  private CDTPParams buildSendGroupMsgParams(CDTPPacket packet) {
    CDTPPacket originalPacket = unpack(packet.getData());
    CDTPParams params = gson.fromJson(new String(originalPacket.getData()), CDTPParams.class);
    params.getBody().put("meta", originalPacket.getHeader());
    params.getBody().put("packet", encode(packet.getData()));
    return params;
  }

  private String encode(byte[] data) {
    return Base64.getUrlEncoder().encodeToString(data);
  }
}
