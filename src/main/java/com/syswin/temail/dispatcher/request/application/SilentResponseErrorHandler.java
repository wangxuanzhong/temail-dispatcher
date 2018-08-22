package com.syswin.temail.dispatcher.request.application;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class SilentResponseErrorHandler extends DefaultResponseErrorHandler {

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) {
  }
}
