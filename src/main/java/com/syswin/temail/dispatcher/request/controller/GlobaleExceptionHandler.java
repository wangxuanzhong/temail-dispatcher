package com.syswin.temail.dispatcher.request.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobaleExceptionHandler {

  private Gson gson = new Gson();

  @ExceptionHandler(DispatchException.class)
  @ResponseStatus(BAD_REQUEST)
  public Response<CDTPPacketTrans> handleException(DispatchException ex) {
    log.error("无效请求的参数：{}", gson.toJson(ex.getPacket()));
    log.error("无效的请求", ex);
    CDTPPacketTrans packet = ex.getPacket();
    Map<String, String> map = new HashMap<>(1);
    map.put("errorMsg", ex.getMessage());
    packet.setData(gson.toJson(map));
    return Response.failed(BAD_REQUEST, ex.getMessage(), packet);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  public Response<String> handleResourceOccupiedException(Exception ex) {
    log.error("服务器请求异常", ex);
    return Response.failed(INTERNAL_SERVER_ERROR, ex.getMessage());
  }

}
