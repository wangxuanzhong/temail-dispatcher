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
      ResponseEntity<String> responseEntity = packageDispatcher.dispatch(reqPackage);
      CDTPPackage respPackage = new CDTPPackage(reqPackage);
      respPackage.setData(responseEntity.getBody());

      return new ResponseEntity<>(Response.ok(responseEntity.getStatusCode(), respPackage),
          responseEntity.getStatusCode());
    } catch (Exception e) {
      throw new DispatchException(e, reqPackage);
    }
  }
}
