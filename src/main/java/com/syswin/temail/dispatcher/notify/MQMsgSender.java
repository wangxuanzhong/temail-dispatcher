package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import java.util.List;

/**
 * @author 姚华成
 * @date 2018-9-5
 */
public interface MQMsgSender {

  void send(List<MqMessage> mqMessages) throws Exception;
}
