package com.syswin.temail.dispatcher.notify.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage {

  String gatewayFlag;
  String instanceFlag;
  String body;
}
