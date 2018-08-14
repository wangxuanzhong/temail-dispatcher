package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.syswin.temail.cdtp.dispatcher.request.application.AuthService;
import com.syswin.temail.cdtp.dispatcher.request.application.Response;
import com.syswin.temail.cdtp.dispatcher.request.entity.AuthData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//@Api("CDTP认证服务")
@RestController
public class AuthController {

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

//  @ApiOperation("CDTP认证服务")
  @PostMapping(value = "/verify")
  public ResponseEntity<Response<String>> verify(
      @RequestBody AuthData body) {
    return authService.verify(body);
  }

}
