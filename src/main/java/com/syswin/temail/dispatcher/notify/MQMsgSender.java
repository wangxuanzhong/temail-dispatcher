package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import java.util.List;

public interface MQMsgSender {

  void send(List<MqMessage> mqMessages) throws Exception;
}
