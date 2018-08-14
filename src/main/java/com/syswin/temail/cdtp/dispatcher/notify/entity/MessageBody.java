package com.syswin.temail.cdtp.dispatcher.notify.entity;

import java.util.Map;
import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
@Data
public class MessageBody {

  private String toTemail;
  private String header;
  private Map<String,Object> data;
}
