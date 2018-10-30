package com.syswin.temail.dispatcher.request.entity;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.Data;

@Data
@JsonNaming
public class CDTPParams {

  private Map<String, String> header;
  private Map<String, String> query;
  private Map<String, Object> path;
  private Map<String, Object> body;

  public CDTPParams() {
  }

  public CDTPParams(Map<String, Object> body) {
    this.body = body;
  }
}
