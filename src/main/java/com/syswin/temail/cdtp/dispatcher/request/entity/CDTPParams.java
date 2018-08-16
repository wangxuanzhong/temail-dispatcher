package com.syswin.temail.cdtp.dispatcher.request.entity;

import java.util.Map;
import lombok.Data;

@Data
public class CDTPParams {

  private Map<String, String> header;
  private Map<String, String> query;
  private Map<String, Object> body;
}
