package com.syswin.temail.dispatcher.request.entity;

import java.util.Map;
import lombok.Data;

@Data
public class CDTPParams {

  private Map<String, String> header;
  private Map<String, String> query;
  private Map<String, Object> body;

  public CDTPParams() {
  }

  public CDTPParams(Map<String, Object> body) {
    this.body = body;
  }
}
