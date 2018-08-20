package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.google.gson.Gson;
import com.syswin.temail.cdtp.dispatcher.request.application.AuthService;
import com.syswin.temail.cdtp.dispatcher.request.entity.AuthData;
import com.syswin.temail.cdtp.dispatcher.request.exceptions.AuthException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api("CDTP认证服务")
@RestController
public class AuthController {

  private Gson gson = new Gson();
  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @ApiOperation("CDTP认证服务")
  @PostMapping(value = "/verify")
  public ResponseEntity<Response<String>> verify(
      @RequestBody AuthData body) {
    try {
      log.info("verify服务接收到的请求信息为：{}", gson.toJson(body));
      ResponseEntity<Response<String>> result = authService.verify(body);
      log.info("verify服务返回的结果为：{}", gson.toJson(result));
      return result;
    } catch (Exception e) {
      throw new AuthException(e);
    }
  }
}
