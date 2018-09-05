package com.syswin.temail.dispatcher.request.application;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

class TemailRequest {

  private final String url;
  private final HttpMethod method;
  private final HttpEntity<?> entity;

  TemailRequest(String url, HttpMethod method, HttpEntity<?> entity) {
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

  HttpEntity<?> entity() {
    return entity;
  }
}
