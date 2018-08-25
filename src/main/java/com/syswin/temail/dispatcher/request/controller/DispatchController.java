package com.syswin.temail.dispatcher.request.controller;

import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.entity.CDTPPacket;
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

  @Autowired
  public DispatchController(PackageDispatcher packageDispatcher) {
    this.packageDispatcher = packageDispatcher;
  }

  @ApiOperation("请求转发")
  @PostMapping(value = "/dispatch")
  public ResponseEntity<Response<CDTPPacket>> dispatch(@RequestBody CDTPPacket cdtpPacket) {
    try {
      log.info("dispatch服务接收到的请求信息为：{}", cdtpPacket);
      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(cdtpPacket);
      cdtpPacket.setData(responseEntity.getBody().getBytes());

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
