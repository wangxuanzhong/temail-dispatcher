package com.syswin.temail.dispatcher.request.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import com.syswin.temail.dispatcher.request.service.DispatcherService;
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

  private final DispatcherService dispatcherService;

  @Autowired
  public DispatchController(DispatcherService dispatcherService) {
    this.dispatcherService = dispatcherService;
  }

  @ApiOperation("CDTP认证服务")
  @PostMapping(value = "/verify", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Response<String>> verify(@RequestBody byte[] payload) throws Exception {
    return this.dispatcherService.verify(payload);
  }

  @ApiOperation("CDTP请求转发")
  @PostMapping(value = "/dispatch", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> dispatch(@RequestBody byte[] payload) throws Exception {
    return this.dispatcherService.dispatch(payload);
  }

  private <T> ResponseEntity<T> repackageResponse(ResponseEntity<T> responseEntity) {
    return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
  }
}
