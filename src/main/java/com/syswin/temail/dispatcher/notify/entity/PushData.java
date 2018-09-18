package com.syswin.temail.dispatcher.notify.entity;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushData extends PushMessage {

  private Integer eventType;

}
