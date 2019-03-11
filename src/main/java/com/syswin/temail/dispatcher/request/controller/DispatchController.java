package com.syswin.temail.dispatcher.request.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import com.syswin.temail.dispatcher.codec.RawPacketDecoder;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api("CDTP分发服务")
@Slf4j
@RestController
public class DispatchController {

  private final PackageDispatcher packageDispatcher;
  private final AuthService authService;
  private final RawPacketDecoder packetDecoder;

  @Autowired
  public DispatchController(PackageDispatcher packageDispatcher,
      AuthService authService,
      RawPacketDecoder packetDecoder) {
    this.packageDispatcher = packageDispatcher;
    this.authService = authService;
    this.packetDecoder = packetDecoder;
  }

  @ApiOperation("CDTP认证服务")
  @PostMapping(value = "/verify", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Response<String>> verify(@RequestBody byte[] payload) throws Exception {
    CDTPPacket packet = packetDecoder.decode(payload);
    try {
      log.info("Dispatcher receive a login verify request：{}", packet);
      ResponseEntity<Response<String>> responseEntity = authService.verify(packet);
      ResponseEntity<Response<String>> result = repackageResponse(responseEntity);
      log.info("Login request packetId: {}, sender: {} verify result: {}-{}", packet.getHeader().getPacketId(),
          packet.getHeader().getSender(), String.valueOf(result.getStatusCode()), result.getBody());
      return result;
    } catch (Exception e) {
      log.error("PacketId: {} verify failed! ", packet.getHeader().getPacketId(), e);
      throw e;
    }

  }

  @ApiOperation("CDTP请求转发")
  @PostMapping(value = "/dispatch", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> dispatch(@RequestBody byte[] payload) throws Exception {
    CDTPPacket packet = packetDecoder.decode(payload);
    try {
      log.info("Dispatcher receive a dispatch request：{}", packet);
      ResponseEntity<Response<String>> verifyResult = authService.verify(packet);
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
