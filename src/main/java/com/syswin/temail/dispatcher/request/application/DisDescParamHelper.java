package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisDescParamHelper {

  private boolean needEncode(CDTPPacket packet) {
    //when command is 13000，do not encode
    return !(packet.getCommandSpace() == 1 && packet.getCommand() == 0x3000);
  }

  public void encodeParam(CDTPPacket packet, CDTPParams params) {
    if (!needEncode(packet)) {
      return;
    }
    if (params.getBody() == null || params.getBody().get("meta") == null) {
      return;
    }
    try {
      Encoder urlEncoder = Base64.getUrlEncoder();
      CDTPHeader header = (CDTPHeader) params.getBody().get("meta");
      String at = header.getAt();
      String topic = header.getTopic();
      String extraData = header.getExtraData();
      if (at != null) {
        header.setAt(urlEncoder.encodeToString(at.getBytes()));
      }
      if (topic != null) {
        header.setTopic(urlEncoder.encodeToString(topic.getBytes()));
      }
      if (extraData != null) {
        header.setExtraData(urlEncoder.encodeToString(extraData.getBytes()));
      }
    } catch (Exception e) {
      log.error("encode dispatcher param error! , param is {}", params, e);
    }
  }

  public void decodeHeader(MessageBody messageBody, CDTPHeader header) {
    if (!needDecode(messageBody)) {
      return;
    }
    try {
      Decoder urlDecoder = Base64.getUrlDecoder();
      String at = header.getAt();
      String topic = header.getTopic();
      String extraData = header.getExtraData();
      if (at != null) {
        header.setAt(new String(urlDecoder.decode(at.getBytes())));
      }
      if (topic != null) {
        header.setTopic(new String(urlDecoder.decode(topic.getBytes())));
      }
      if (extraData != null) {
        header.setExtraData(new String(urlDecoder.decode(extraData.getBytes())));
      }
      log.info("decoded header is {}", header);
    } catch (Exception e) {
      log.error("decode mq message header error, header is {}", header, e);
    }

  }

  private boolean needDecode(MessageBody body) {
    return (body.getEventType() != 52); //flag 13000 command
  }
}
