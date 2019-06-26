/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.dispatcher.request.application;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.dispatcher.request.exceptions.HttpAccessException;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
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
    try {
      return restTemplate.exchange(request.url(),
          request.method(), request.entity(), String.class);
    } catch (RestClientException e) {
      throw new HttpAccessException(e, request);
    }
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
