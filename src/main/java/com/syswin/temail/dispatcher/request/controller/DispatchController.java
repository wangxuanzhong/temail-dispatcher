package com.syswin.temail.dispatcher.request.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import com.syswin.temail.dispatcher.codec.RawPacketDecoder;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
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
  public ResponseEntity<Response<String>> verify(@RequestBody byte[] payload) {
    CDTPPacket packet = packetDecoder.decode(payload);
    log.debug("verify service receive a request：{}", packet);
    ResponseEntity<Response<String>> responseEntity = authService.verify(packet);
    ResponseEntity<Response<String>> result = repackageResponse(responseEntity);
    log.debug("verify result：{}", result.getBody());
    return result;
  }

  @ApiOperation("CDTP请求转发")
  @PostMapping(value = "/dispatch", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> dispatch(@RequestBody byte[] payload) {
    CDTPPacket packet = packetDecoder.decode(payload);
    try {
      ResponseEntity<Response<String>> verifyResult = authService.verify(packet);

      if (verifyResult.getStatusCode().is2xxSuccessful()) {
        log.debug("dispatch service receive a request：{}", packet);
        ResponseEntity<String> responseEntity = packageDispatcher.dispatch(packet);
        ResponseEntity<String> result = repackageResponse(responseEntity);
        log.debug("dispatch result：{}", result);
        return result;
      }

      log.error("signature verify fail! param: {}", packet);
      return repackageResponse(verifyResult);
    } catch (DispatchException e) {
      throw e;
    } catch (Exception e) {
      throw new DispatchException(e, packet);
    }
  }

  private <T> ResponseEntity<T> repackageResponse(ResponseEntity<T> responseEntity) {
    return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
  }
}
