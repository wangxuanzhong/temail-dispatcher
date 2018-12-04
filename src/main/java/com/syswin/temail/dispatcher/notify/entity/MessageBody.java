package com.syswin.temail.dispatcher.notify.entity;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
@Data
public class MessageBody {

  private String receiver;
  private String header;
  private String data;
  private Integer eventType;
}
