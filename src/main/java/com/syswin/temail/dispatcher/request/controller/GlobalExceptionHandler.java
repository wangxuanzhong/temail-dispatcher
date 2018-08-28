package com.syswin.temail.dispatcher.request.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  Response<String> handleUnexpectedException(Exception e) {
    return Response.failed(INTERNAL_SERVER_ERROR, e.getMessage());
  }
}
