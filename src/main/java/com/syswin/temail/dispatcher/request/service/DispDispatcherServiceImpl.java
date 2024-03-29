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

package com.syswin.temail.dispatcher.request.service;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.dispatcher.request.application.DispAuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.application.RequestFactory;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.dispatcher.request.exceptions.HttpAccessException;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

@Slf4j
public class DispDispatcherServiceImpl implements DispDispatcherService {

  private final Gson gson = new Gson();

  private final PackageDispatcher packageDispatcher;

  private final DispAuthService dispAuthService;

  private final DispRawPacketDecoder packetDecoder;

  public DispDispatcherServiceImpl(PackageDispatcher packageDispatcher,
      DispAuthService dispAuthService, DispRawPacketDecoder packetDecoder) {
    this.packageDispatcher = packageDispatcher;
    this.dispAuthService = dispAuthService;
    this.packetDecoder = packetDecoder;
  }

  @Override
  public ResponseEntity<Response<String>> verify(byte[] payload) throws Exception {
    CDTPPacket packet = packetDecoder.decode(payload);
    try {
      ResponseEntity<Response<String>> responseEntity = dispAuthService.verify(packet);
      ResponseEntity<Response<String>> result = repackageResponsetoStr(responseEntity);
      log.info("Login request by sender: {} with packetId: {} verify result: {}-{}",
          packet.getHeader().getSender(),
          packet.getHeader().getPacketId(), String.valueOf(result.getStatusCode()),
          result.getBody());
      return result;
    } catch (Exception e) {
      log.error("PacketId: {} verify failed! ", packet.getHeader().getPacketId(), e);
      throw e;
    }

  }

  @Override
  public ResponseEntity<String> dispatch(byte[] payload) throws Exception {
    CDTPPacket packet = packetDecoder.decode(payload);
    try {
      ResponseEntity<Response<String>> verifyResult = dispAuthService.verify(packet);
      log.info("Dispatch request packetId: {}, sender: {} verify result: {}-{}",
          packet.getHeader().getPacketId(), packet.getHeader().getSender(),
          String.valueOf(verifyResult.getStatusCode()), verifyResult.getBody());

      if (verifyResult.getStatusCode().is2xxSuccessful()) {
        ResponseEntity<String> responseEntity = packageDispatcher.dispatch(packet);
        ResponseEntity<String> result = repackageResponsetoStr(responseEntity);
        log.info("PacketId: {} dispatch result：{}", packet.getHeader().getPacketId(), result);
        return result;
      }
      return repackageResponse(verifyResult);
    } catch (DispatchException e) {
      if (e.getMessage() != null && e.getMessage().startsWith(RequestFactory.UNSUPPORTED_CMD_PREfIX)
          && !StringUtils.isEmpty(packageDispatcher.getDispatcherProperties().getMockUrl())) {
        return packageDispatcher.forwardToMockApi(payload, e);
      } else {
        throw e;
      }
    } catch (Exception e) {
      if (e instanceof HttpAccessException) {
        log.error("PacketId: {} dispatch failed, request info: {}",
            packet.getHeader().getPacketId(), ((HttpAccessException) e).getTemailRequest(), e);
      } else {
        log.error("PackedId: {} dispatch failed! ",
            packet.getHeader().getPacketId(), e);
      }
      throw e;
    }
  }

  private ResponseEntity<String> repackageResponse(ResponseEntity<Response<String>> verifyResult) {
    return new ResponseEntity<>(gson.toJson(verifyResult.getBody()), verifyResult.getStatusCode());
  }

  private <T> ResponseEntity<T> repackageResponsetoStr(ResponseEntity<T> responseEntity) {
    return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
  }

}
