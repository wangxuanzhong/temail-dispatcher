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

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.valid.PacketValidJudge;
import com.syswin.temail.dispatcher.valid.params.ValidParams;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class DispAuthService {

  static final ResponseEntity<Response<String>> ALWAYS_SUCCESS =
      new ResponseEntity<Response<String>>(Response.ok(HttpStatus.OK, "Success"), HttpStatus.OK);
  static final ValidParams EMPTY_VALID_PARAMS = new ValidParams();

  private final RestTemplate restTemplate;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();
  private final CommandAwarePacketUtil packetUtil;
  private final PacketValidJudge packetValidJudge;
  private DispatcherProperties dispatcherProperties;

  public DispAuthService(RestTemplate restTemplate, DispatcherProperties dispatcherProperties,
      CommandAwarePacketUtil packetUtil, PacketValidJudge packetValidJudge) {
    this.restTemplate = restTemplate;
    this.packetUtil = packetUtil;
    this.packetValidJudge = packetValidJudge;
    this.dispatcherProperties = dispatcherProperties;
    this.headers.setContentType(APPLICATION_FORM_URLENCODED);
  }

  public ResponseEntity<Response<String>> verify(CDTPPacket packet) {
    Optional<ValidParams> validParams = this.packetValidJudge.buildParams(packet, packetUtil::extractUnsignedData);
    log.info("PackedId: {} verify params is: {} !", packet.getHeader().getPacketId(),
        validParams.orElse(EMPTY_VALID_PARAMS).toString());
    if (!validParams.isPresent() || !(validParams.get().isAnValidParam())) {
      return ALWAYS_SUCCESS;
    } else {
      return verify(validParams);
    }
  }

  private ResponseEntity<Response<String>> verify(Optional<ValidParams> validParams) {
    MultiValueMap<String, String> entityBody = new LinkedMultiValueMap<>();
    for (Map.Entry<String, String> entry : validParams.get().getParams().entrySet()) {
      entityBody.add(entry.getKey(), entry.getValue());
    }
    HttpEntity<?> requestEntity = new HttpEntity<>(entityBody, headers);
    ResponseEntity<Response<String>> result = restTemplate
        .exchange(dispatcherProperties.getAuthBaseUrl() + validParams.get().getAuthUri(),
            POST, requestEntity, responseType);
    return result;
  }

  private ParameterizedTypeReference<Response<String>> responseType() {
    return new ParameterizedTypeReference<Response<String>>() {
    };
  }
}
