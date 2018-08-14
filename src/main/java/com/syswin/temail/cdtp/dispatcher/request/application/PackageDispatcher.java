package com.syswin.temail.cdtp.dispatcher.request.application;

import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class PackageDispatcher {

  private final RequestFactory requestFactory;
  private final RestTemplate restTemplate;

  public PackageDispatcher(DispatcherProperties properties, RestTemplate restTemplate) {
    this.requestFactory = new RequestFactory(properties);
    this.restTemplate = restTemplate;
  }

  public ResponseEntity<String> dispatch(CDTPPackage cdtpPackage) {
    TeMailRequest request = requestFactory.toRequest(cdtpPackage);
    return restTemplate.exchange(request.url(), request.method(), request.entity(), String.class);
  }
}
