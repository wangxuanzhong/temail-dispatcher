package com.syswin.temail.dispatcher.request.application;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static com.syswin.temail.ps.common.utils.PacketUtil.unpack;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.utils.PacketUtil;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-11-05
 */
@Slf4j
public class CommandAwarePacketUtil implements CDTPPacketUtil {

  private static Gson gson = new Gson();
  private final DispatcherProperties properties;
  private final CDTPPacketUtil defaultPacketUtil = new SimplePacketUtil();

  public CommandAwarePacketUtil(DispatcherProperties properties) {
    this.properties = properties;
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

  @Override
  public byte[] decodeData(CDTPPacketTrans packet) {
    String data;
    if (packet == null || (data = packet.getData()) == null) {
      return new byte[0];
    }
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
      return Base64.getUrlDecoder().decode(data);
    } else {
      return defaultPacketUtil.decodeData(packet);
    }
  }

  public byte[] decodeOriginalData(CDTPPacketTrans packet) {
    byte[] packetBytes;
    if ((packetBytes = decodeData(packet)) == null) {
      return new byte[0];
    }
    CDTPPacket originalPacket = unpack(packetBytes);
    return originalPacket.getData();
  }

  @Override
  public CDTPParams buildParams(CDTPPacketTrans packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    try {
      if (isSendSingleMsg(commandSpace, command)) {
        return buildSendSingleMsgParams(packet);
      } else if (isSendGroupMsg(commandSpace, command)) {
        return buildSendGroupMsgParams(packet);
      } else {
        return defaultPacketUtil.buildParams(packet);
      }
    } catch (JsonSyntaxException e) {
      log.error("Body的Json格式解析错误，请求参数：{}", packet);
      throw new DispatchException(e, packet);
    }
  }

  boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  private boolean isSendGroupMsg(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1) &&
        properties.isGroupPacketEnabled();
  }

  boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 0x0107;
  }

  private CDTPParams buildSendSingleMsgParams(CDTPPacketTrans packet) {
    CDTPHeader header = packet.getHeader();
    Map<String, Object> extraData = gson
        .fromJson(header.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());
    Map<String, Object> body = new HashMap<>(extraData);
    body.put("sender", header.getSender());
    body.put("receiver", header.getReceiver());
    body.put("msgData", packet.getData());

    return new CDTPParams(body);
  }

  private CDTPParams buildSendGroupMsgParams(CDTPPacketTrans packet) {
    CDTPPacket originalPacket = PacketUtil.unpack(packet.getData());
    CDTPParams params = gson.fromJson(new String(originalPacket.getData()), CDTPParams.class);
    params.getBody().put("meta", originalPacket.getHeader());
    params.getBody().put("packet", packet.getData());
    return params;
  }
}
