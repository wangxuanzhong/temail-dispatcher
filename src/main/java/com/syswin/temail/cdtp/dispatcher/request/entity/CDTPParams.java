package com.syswin.temail.cdtp.dispatcher.request.entity;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CDTPParams {
  private Map<String, List<String>> header;
  private Map<String, List<String>> query;
  private Map<String, Object> body;
}
