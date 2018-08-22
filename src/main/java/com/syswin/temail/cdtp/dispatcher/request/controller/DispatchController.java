package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.syswin.temail.cdtp.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import com.syswin.temail.cdtp.dispatcher.request.exceptions.DispatchException;
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
  public ResponseEntity<Response<CDTPPackage>> dispatch(@RequestBody CDTPPackage reqPackage) {
    try {
      log.info("dispatch服务接收到的请求信息为：{}", reqPackage);
      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(reqPackage);
      CDTPPackage respPackage = new CDTPPackage(reqPackage);
      respPackage.setData(responseEntity.getBody());

      ResponseEntity<Response<CDTPPackage>> result = new ResponseEntity<>(
          Response.ok(responseEntity.getStatusCode(), respPackage),
          responseEntity.getStatusCode());
      log.info("dispatch服务返回的结果为：{}", result);
      return result;
    } catch (DispatchException e) {
      throw e;
    } catch (Exception e) {
      throw new DispatchException(e, reqPackage);
    }
  }
}
