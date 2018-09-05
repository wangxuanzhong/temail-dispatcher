package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class PackageDispatcher {

  private final RestTemplate restTemplate;
  private RequestFactory requestFactory;

  public PackageDispatcher(DispatcherProperties properties, RestTemplate restTemplate) {
    this.requestFactory = new RequestFactory(properties);
    this.restTemplate = restTemplate;
  }

  public ResponseEntity<String> dispatch(CDTPPacketTrans packet) {
    TemailRequest request = requestFactory.toRequest(packet);
    return restTemplate.exchange(request.url(), request.method(), request.entity(), String.class);
  }

}
