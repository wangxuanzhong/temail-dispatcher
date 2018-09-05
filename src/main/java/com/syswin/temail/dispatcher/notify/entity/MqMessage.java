package com.syswin.temail.dispatcher.notify.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-9-5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage {

  String gatewayFlag;
  String instanceFlag;
  String body;
}
