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
public class NotificationMessageFactory {

  private final Gson gson;

  public NotificationMessageFactory() {
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
      Map pushOptions = this.extractPushOptions(header);
      pushMsg.setCmd(String.valueOf(pushOptions.get("cmd")));
      pushMsg.setType(String.valueOf(pushOptions.get("type")));
      return Optional.ofNullable(gson.toJson(pushMsg));
    } catch (Exception e) {
      log.error("failed to extract push data", e);
      return Optional.empty();
    }
  }

  public Map extractPushOptions(CDTPHeader header) {
    try {
      Map extraOption = getExtraData(header);
      return (Map) (extraOption.getOrDefault("push", emptyMap()));
    } catch (Exception e) {
      log.error("fail to extract cmd and type from CDTPHeader, extraData : {}", header.getExtraData(), e);
      return emptyMap();
    }
  }

  public Map getExtraData(CDTPHeader header) {
    return Optional.ofNullable(gson.fromJson(
        header.getExtraData(), Map.class)).orElse(emptyMap());
  }


}

