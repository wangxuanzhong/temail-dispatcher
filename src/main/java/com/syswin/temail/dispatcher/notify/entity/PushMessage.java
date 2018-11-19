package com.syswin.temail.dispatcher.notify.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage implements Serializable {

  private String msgId;

  private String from;

  private String to;

  private String message;

  private String type;

  private String cmd;

}
