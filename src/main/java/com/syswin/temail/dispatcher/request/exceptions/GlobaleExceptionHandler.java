package com.syswin.temail.dispatcher.request.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobaleExceptionHandler {

  private Gson gson = new Gson();

  @ExceptionHandler(AuthException.class)
  @ResponseStatus(BAD_REQUEST)
  public Response<String> handleException(AuthException ex) {
    log.error("无效的请求", ex);
    return Response.failed(BAD_REQUEST, ex.getMessage());
  }

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
