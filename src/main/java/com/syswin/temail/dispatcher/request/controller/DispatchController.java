package com.syswin.temail.dispatcher.request.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.entity.AuthData;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
  private final Gson gson = new Gson();

  @Autowired
  public DispatchController(PackageDispatcher packageDispatcher,
      AuthService authService) {
    this.packageDispatcher = packageDispatcher;
    this.authService = authService;
  }

  @ApiOperation("CDTP认证服务")
  @PostMapping(value = "/verify", consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<Response<String>> verify(@RequestBody AuthData body) {
    log.debug("verify服务接收到的请求信息为：{}", gson.toJson(body));
    ResponseEntity<Response<String>> result = authService.verify(
        body.getTemail(),
        body.getUnsignedBytes(),
        body.getSignature(),
        body.getAlgorithm());
    log.debug("verify服务返回的结果为：{}", gson.toJson(result));
    return result;
  }

  @ApiOperation("CDTP请求转发")
  @PostMapping(value = "/dispatch", consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<String> dispatch(@RequestBody CDTPPacketTrans packet) {
    try {
      ResponseEntity<Response<String>> verifyResult = authService.verify(packet);

      if (verifyResult.getStatusCode().is2xxSuccessful()) {
        log.debug("dispatch服务接收到的请求信息为：{}", packet);
        ResponseEntity<String> responseEntity = packageDispatcher.dispatch(packet);
        ResponseEntity<String> result = new ResponseEntity<>(responseEntity.getBody(),
            responseEntity.getStatusCode());
        log.debug("dispatch服务返回的结果为：{}", result);
        return result;
      } else {
        log.error("签名数据验证失败! 请求参数：{}", packet);
        Response<String> resultBody = verifyResult.getBody();
        String errorMesage;
        if (resultBody != null) {
          errorMesage = resultBody.getMessage();
        } else {
          errorMesage = "数据包签名验证未通过!";
        }
        return new ResponseEntity<>(gson.toJson(Response.failed(verifyResult.getStatusCode(), errorMesage)),
            HttpStatus.FORBIDDEN);
      }
    } catch (DispatchException e) {
      throw e;
    } catch (Exception e) {
      throw new DispatchException(e, packet);
    }
  }
}
