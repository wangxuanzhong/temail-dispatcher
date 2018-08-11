package com.syswin.temail.cdtp.dispatcher.receive.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.OK;

@JsonInclude(Include.NON_NULL)
public class Response<T> {

  private Integer code;
  private String message;
  private T data;

  public static <T> Response<T> ok() {
    return new Response<>(OK);
  }

  static <T> Response<T> ok(T body) {
    return ok(OK, body);
  }

  public static <T> Response<T> ok(HttpStatus status, T body) {
    return new Response<>(status, null, body);
  }

  public static <T> Response<T> failed(HttpStatus status) {
    return new Response<>(status);
  }

  public static <T> Response<T> failed(HttpStatus status, String message) {
    return new Response<>(status, message);
  }

  static <T> Response<T> failed(HttpStatus status, String message, T body) {
    return new Response<>(status, message, body);
  }

  private Response() {
  }

  private Response(HttpStatus status) {
    this.code = status.value();
  }

  private Response(HttpStatus status, String message) {
    this.code = status.value();
    this.message = message;
  }

  private Response(HttpStatus status, String message, T data) {
    this.code = status.value();
    this.message = message;
    this.data = data;
  }

  public Integer getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }
}
