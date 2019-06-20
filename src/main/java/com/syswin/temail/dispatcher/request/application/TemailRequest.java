package com.syswin.temail.dispatcher.request.application;

import java.util.Map;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@Data
public class TemailRequest {

  private final String url;
  private final HttpMethod method;
  private final HttpEntity<Map<String, Object>> entity;

  public TemailRequest(String url, HttpMethod method, HttpEntity<Map<String, Object>> entity) {
    this.url = url;
    this.method = method;
    this.entity = entity;
  }

  String url() {
    return url;
  }

  HttpMethod method() {
    return method;
  }

  HttpEntity<Map<String, Object>> entity() {
    return entity;
  }
}
