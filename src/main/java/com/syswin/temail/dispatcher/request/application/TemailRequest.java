package com.syswin.temail.dispatcher.request.application;

import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

class TemailRequest {

  private final String url;
  private final HttpMethod method;
  private final HttpEntity<Map<String, Object>> entity;

  TemailRequest(String url, HttpMethod method, HttpEntity<Map<String, Object>> entity) {
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
