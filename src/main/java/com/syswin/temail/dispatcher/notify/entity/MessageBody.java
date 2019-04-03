package com.syswin.temail.dispatcher.notify.entity;

import lombok.Data;


@Data
public class MessageBody {

  private String receiver;
  private String header;
  private String data;
  private Integer eventType;
}
