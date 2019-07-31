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

package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND_SPACE;
import static java.util.Collections.emptyMap;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.Constants;
import com.syswin.temail.dispatcher.notify.entity.PushData;
import com.syswin.temail.dispatcher.notify.entity.PushMessage;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.zookeeper.Op.Delete;
import org.springframework.beans.BeanUtils;

@Slf4j
public class NotificationMessageFactory {

  private final Gson gson;

  public NotificationMessageFactory() {
    gson = new Gson();
  }

  public String notificationOf(String receiver, CDTPHeader header, String body) {
    CDTPPacketTrans packet = new CDTPPacketTrans();
    packet.setCommandSpace(NOTIFY_COMMAND_SPACE);
    packet.setCommand(NOTIFY_COMMAND);
    packet.setVersion(CDTP_VERSION);
    header.setReceiver(receiver);
    packet.setHeader(header);
    packet.setData(body);
    return gson.toJson(packet);
  }

  public Optional<String> getPushMessage(String receiver, CDTPHeader header, String body) {
    try {
      PushData pushData = gson.fromJson(body, PushData.class);
      PushMessage pushMsg = new PushMessage();
      BeanUtils.copyProperties(pushData, pushMsg);
      if(pushData.getEventType() == Constants.GROUP_MSG_EVENT_TYPE){
        pushMsg.setFrom(pushData.getGroupTemail());
      }
      Map pushOptions = this.extractPushOptions(header);
      pushMsg.setCmd(pushOptions.get("cmd") == null ? null : pushOptions.get("cmd").toString());
      pushMsg.setType(pushOptions.get("type") == null ? null : pushOptions.get("type").toString());
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

