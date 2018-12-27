package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND_SPACE;
import static java.util.Collections.emptyMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.dispatcher.notify.entity.PushMessage;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Slf4j
class NotificationMessageFactory {

  private final Gson gson;

  NotificationMessageFactory() {
    gson = new Gson();
  }

  String notificationOf(String receiver, CDTPHeader header, String body) {
    CDTPPacketTrans packet = new CDTPPacketTrans();
    packet.setCommandSpace(NOTIFY_COMMAND_SPACE);
    packet.setCommand(NOTIFY_COMMAND);
    packet.setVersion(CDTP_VERSION);
    header.setReceiver(receiver);
    packet.setHeader(header);
    packet.setData(body);
    return gson.toJson(packet);
  }

  Optional<String> getPushMessage(String receiver, CDTPHeader header, String body) {
    try {
      PushData pushData = gson.fromJson(body, PushData.class);
      PushMessage pushMsg = new PushMessage();
      BeanUtils.copyProperties(pushData, pushMsg);
      Map<String, String> pushOptions = this.extractPushOptions(header);
      pushMsg.setCmd(pushOptions.get("cmd"));
      pushMsg.setType(pushOptions.get("type"));
      return Optional.ofNullable(gson.toJson(pushMsg));
    } catch (Exception e) {
      log.error("failed to extract push data", e);
      return Optional.empty();
    }
  }

  Map<String, String> extractPushOptions(CDTPHeader header) {
    try {
      Map extraOption = Optional.ofNullable(gson.fromJson(header.getExtraData(), Map.class)).orElse(emptyMap());
      Map result = (Map) extraOption.getOrDefault("push", emptyMap());
      return result;
    } catch (Exception e) {
      log.error("fail to extract cmd and type from CDTPHeader, extraData : {}", header.getExtraData(), e);
      return emptyMap();
    }
  }
}

