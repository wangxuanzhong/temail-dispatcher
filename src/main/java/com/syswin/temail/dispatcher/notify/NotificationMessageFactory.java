package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND_SPACE;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.dispatcher.notify.entity.PushMessage;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans.Header;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

class NotificationMessageFactory {

  private final Gson gson;

  NotificationMessageFactory() {
    gson = new Gson();
  }

  String notificationOf(String receiver, Header header, String body) {
    CDTPPacketTrans packet = new CDTPPacketTrans();
    packet.setCommandSpace(NOTIFY_COMMAND_SPACE);
    packet.setCommand(NOTIFY_COMMAND);
    packet.setVersion(CDTP_VERSION);
    header.setReceiver(receiver);
    packet.setHeader(header);
    packet.setData(body);
    return gson.toJson(packet);
  }

  Optional<String> getPushMessage(String receiver, Header header, String body) {

    PushData pushData = gson.fromJson(body, PushData.class);
    if (pushData.getEventType().equals(0)) {
      PushMessage pushMsg = new PushMessage();
      BeanUtils.copyProperties(pushData, pushMsg);
      return Optional.ofNullable(gson.toJson(pushMsg));
    }
    return Optional.empty();
  }
}
