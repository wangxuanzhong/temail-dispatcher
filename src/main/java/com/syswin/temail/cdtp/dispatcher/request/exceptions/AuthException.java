package com.syswin.temail.cdtp.dispatcher.request.exceptions;

/**
 * @author 姚华成
 * @date 2018-8-15
 */
public class AuthException extends RuntimeException {

  public AuthException() {
  }

  public AuthException(String message) {
    super(message);
  }

  public AuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthException(Throwable cause) {
    super(cause);
  }

  public AuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
