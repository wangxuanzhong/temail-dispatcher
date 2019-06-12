package com.syswin.temail.dispatcher.request.application;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class PackageDispatcher {

  private final RestTemplate restTemplate;
  private RequestFactory requestFactory;
  @Getter
  private DispatcherProperties dispatcherProperties;

  public PackageDispatcher(DispatcherProperties properties,
      RestTemplate restTemplate, RequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    this.dispatcherProperties = properties;
    this.restTemplate = restTemplate;
  }

  public ResponseEntity<String> dispatch(CDTPPacket packet) {
    TemailRequest request = requestFactory.toRequest(packet);
    return restTemplate.exchange(request.url(),
        request.method(), request.entity(), String.class);
  }

  public ResponseEntity<String> forwardToMockApi(byte[] bytes, DispatchException e) {
    if (StringUtils.isEmpty(this.dispatcherProperties.getMockUrl())) {
      log.debug("No mockUrl specified, so rethrow the exception: {}", e);
      throw e;
    }
    return restTemplate.exchange(this.dispatcherProperties.getMockUrl(),
        HttpMethod.POST, this.buildBytesEntity(bytes), String.class);
  }

  private HttpEntity buildBytesEntity(byte[] bytes) {
    MultiValueMap multiValueMap = new LinkedMultiValueMap();
    multiValueMap.set(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
    return new HttpEntity(bytes, multiValueMap);
  }

}
