package com.syswin.temail.cdtp.dispatcher.request.application;

import java.io.IOException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class SilentResponseErrorHandler extends DefaultResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
    return super.hasError(clientHttpResponse);
  }

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) {
  }
}
