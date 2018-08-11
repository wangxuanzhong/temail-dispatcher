package com.syswin.temail.cdtp.dispatcher.request.application;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

class TeMailRequest {

  private final String url;
  private final HttpMethod method;
  private final HttpEntity<?> entity;

  TeMailRequest(String url, HttpMethod method, HttpEntity<?> entity) {
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
