package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND_SPACE;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.dispatcher.Constants;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.dispatcher.notify.entity.PushMessage;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.HashMap;
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
    PushData pushData = gson.fromJson(body, PushData.class);
    if (pushData.getEventType().equals(Constants.COMMON_MSG_EVENT_TYPE)) {
      PushMessage pushMsg = new PushMessage();
      BeanUtils.copyProperties(pushData, pushMsg);
      Map<String,String> pushOptions = this.extractPushOptions(header);
      pushMsg.setCmd(Optional.ofNullable(pushOptions.get("cmd")).orElse(""));
      pushMsg.setType(Optional.ofNullable(pushOptions.get("type")).orElse(""));
      return Optional.ofNullable(gson.toJson(pushMsg));
    }
    return Optional.empty();
  }

  //extract push options
  Map<String, String> extractPushOptions(CDTPHeader header) {
    Map<String, String> pushOptions = new HashMap<>();
    try {
      Map<String, Map<String, String>> extraOption = Optional.ofNullable(
          gson.fromJson(header.getExtraData(), Map.class)).orElse(new HashMap());
      pushOptions = Optional.ofNullable(extraOption.get("push")).orElse(new HashMap<>());
    } catch (JsonSyntaxException e) {
      log.error("从CDTPHeader中提取cmd 和 type 失败，extraData : {}", header.getExtraData(), e);
    }
    return pushOptions;
  }


}
