package com.syswin.temail.dispatcher.request.service;

import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.dispatcher.request.application.DispAuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public class DispatcherServiceImpl implements DispatcherService {

  private final PackageDispatcher packageDispatcher;

  private final DispAuthService dispAuthService;

  private final DispRawPacketDecoder packetDecoder;

  public DispatcherServiceImpl(PackageDispatcher packageDispatcher,
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
      ResponseEntity<Response<String>> result = repackageResponse(responseEntity);
      log.info("Login request by sender: {} with packetId: {} verify result: {}-{}", packet.getHeader().getSender(),
          packet.getHeader().getPacketId(), String.valueOf(result.getStatusCode()), result.getBody());
      return result;
    } catch (Exception e) {
      log.error("PacketId: {} verify failed! ", packet.getHeader().getPacketId(), e);
      throw e;
    }

  }

  @Override
  public ResponseEntity<?> dispatch(byte[] payload) throws Exception {
    CDTPPacket packet = packetDecoder.decode(payload);
    try {
      ResponseEntity<Response<String>> verifyResult = dispAuthService.verify(packet);
      log.info("Dispatch request packetId: {}, sender: {} verify result: {}-{}", packet.getHeader().getPacketId(),
          packet.getHeader().getSender(), String.valueOf(verifyResult.getStatusCode()), verifyResult.getBody());
      if (verifyResult.getStatusCode().is2xxSuccessful()) {
        ResponseEntity<String> responseEntity = packageDispatcher.dispatch(packet);
        ResponseEntity<String> result = repackageResponse(responseEntity);
        log.info("PacketId: {} dispatch result：{}", packet.getHeader().getPacketId(), result);
        return result;
      }
      return repackageResponse(verifyResult);
    } catch (Exception e) {
      log.error("PackedId: {} dispatch failed! ", packet.getHeader().getPacketId(), e);
      throw e;
    }
  }

  private <T> ResponseEntity<T> repackageResponse(ResponseEntity<T> responseEntity) {
    return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
  }
}
