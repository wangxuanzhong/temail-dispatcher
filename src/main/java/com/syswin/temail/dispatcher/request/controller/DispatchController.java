package com.syswin.temail.dispatcher.request.controller;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.entity.AuthData;
import com.syswin.temail.dispatcher.request.entity.CDTPPacket;
import com.syswin.temail.dispatcher.request.exceptions.AuthException;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
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
  private Gson gson = new Gson();

  @Autowired
  public DispatchController(PackageDispatcher packageDispatcher,
      AuthService authService) {
    this.packageDispatcher = packageDispatcher;
    this.authService = authService;
  }

  @ApiOperation("CDTP认证服务")
  @PostMapping(value = "/verify")
  public ResponseEntity<Response<String>> verify(@RequestBody AuthData body) {
    try {
      log.info("verify服务接收到的请求信息为：{}", gson.toJson(body));
      ResponseEntity<Response<String>> result = authService.verify(body);
      log.info("verify服务返回的结果为：{}", gson.toJson(result));
      return result;
    } catch (Exception e) {
      throw new AuthException(e);
    }
  }

  @ApiOperation("CDTP请求转发")
  @PostMapping(value = "/dispatch")
  public ResponseEntity<Response<CDTPPacket>> dispatch(@RequestBody CDTPPacket cdtpPacket) {
    try {
      log.info("dispatch服务接收到的请求信息为：{}", cdtpPacket);
      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(cdtpPacket);
      String entityBody = responseEntity.getBody();
      byte[] packetData;
      if (entityBody == null) {
        packetData = new byte[0];
      } else {
        packetData = entityBody.getBytes();
      }
      cdtpPacket.setData(packetData);

      ResponseEntity<Response<CDTPPacket>> result = new ResponseEntity<>(
          Response.ok(responseEntity.getStatusCode(), cdtpPacket),
          responseEntity.getStatusCode());
      log.info("dispatch服务返回的结果为：{}", result);
      return result;
    } catch (DispatchException e) {
      throw e;
    } catch (Exception e) {
      throw new DispatchException(e, cdtpPacket);
    }
  }
}
