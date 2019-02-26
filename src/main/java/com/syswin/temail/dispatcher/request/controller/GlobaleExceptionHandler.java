package com.syswin.temail.dispatcher.request.controller;

import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobaleExceptionHandler {

  private final Gson gson = new Gson();

  @ExceptionHandler(DispatchException.class)
  @ResponseStatus(BAD_REQUEST)
  public Response<String> handleException(DispatchException ex) {
    Map<String, String> map = singletonMap("errorMsg", ex.getMessage());
    return Response.failed(BAD_REQUEST, ex.getMessage(), gson.toJson(map));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  public Response<String> handleResourceOccupiedException(Exception ex) {
    return Response.failed(INTERNAL_SERVER_ERROR, ex.getMessage());
  }

}
