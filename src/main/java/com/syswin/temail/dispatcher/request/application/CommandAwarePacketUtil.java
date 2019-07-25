/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.dispatcher.request.application;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.defaultString;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.dispatcher.request.utils.DigestUtil;
import com.syswin.temail.dispatcher.request.utils.HexUtil;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.packet.PacketUtil;
import com.syswin.temail.ps.common.packet.SimplePacketUtil;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

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
        isSendGroupMsg(commandSpace, command) ||
        isSendNewGroupMsg(commandSpace, command)) {
      CDTPPacket originalPacket = unpack(data);
      data = originalPacket.getData();
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
      } else if (isSendNewGroupMsg(commandSpace, command)) {
        return buildSendNewGroupMsgParams(packet);
      } else {
        return gson.fromJson(new String(packet.getData(), UTF_8), CDTPParams.class);

      }
    } catch (JsonSyntaxException e) {
      log.error("fail to parse json format data of Body, param：{}", packet);
      throw new DispatchException(e, packet);
    }
  }

  private boolean isSendNewGroupMsg(short commandSpace, short command) {
    return packetTypeJudge.isNewGroupMessage(commandSpace, command);
  }

  private CDTPParams buildSendNewGroupMsgParams(CDTPPacket packet) {
    CDTPPacket originalPacket = unpack(packet.getData());
    CDTPParams params = new CDTPParams(new HashMap<>());
    params.getBody().put("meta", originalPacket.getHeader());
    params.getBody().put("packet", encode(packet.getData()));
    return params;
  }

  boolean isBizServerValidType(short commandSpace) {
    return packetTypeJudge.isBizServerValidType(commandSpace);
  }

  boolean isSendSingleMsg(short commandSpace, short command) {
    return packetTypeJudge.isPrivateDecryptType(commandSpace, command);
  }

  private boolean isSendGroupMsg(short commandSpace, short command) {
    return packetTypeJudge.isGroupDecryptType(commandSpace, command);
  }


  private CDTPParams buildSendSingleMsgParams(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    Map<String, Object> extraData = gson
        .fromJson(header.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());
    Map<String, Object> body = new HashMap<>(extraData == null ? Collections.emptyMap() : extraData);
    body.put("msgData", encode(packet.getData()));
    body.put("meta", header);

    return new CDTPParams(body);
  }

  private CDTPParams buildSendGroupMsgParams(CDTPPacket packet) {
    log.info("build data {} ",packet);
    CDTPPacket originalPacket = unpack(packet.getData());
    CDTPParams params = gson.fromJson(new String(originalPacket.getData()), CDTPParams.class);
    log.info("build data param {} ",params);
    params.getBody().put("meta", originalPacket.getHeader());
    params.getBody().put("packet", encode(packet.getData()));
    return params;
  }

  private String encode(byte[] data) {
    return Base64.getUrlEncoder().encodeToString(data);
  }

  public String extractUnsignedData(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    String targetAddress = defaultString(header.getTargetAddress());
    byte[] data = packet.getData();
    String dataSha256 = data == null ? "" :
        HexUtil.encodeHex(
            DigestUtil.sha256(
                this.decodeData(packet)));

    return String.valueOf(packet.getCommandSpace() + packet.getCommand())
        + targetAddress
        + String.valueOf(header.getTimestamp())
        + dataSha256;
  }
}
