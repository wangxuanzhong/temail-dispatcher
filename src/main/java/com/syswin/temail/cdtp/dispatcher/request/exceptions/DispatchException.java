package com.syswin.temail.cdtp.dispatcher.request.exceptions;

import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPHeader;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 姚华成
 * @date 2018-8-15
 */
public class DispatchException extends RuntimeException {

  @Getter
  @Setter
  private CDTPHeader cdtpHeader;

  public DispatchException(CDTPHeader cdtpHeader) {
    setCdtpHeader(cdtpHeader);
  }

  public DispatchException(String message, CDTPHeader cdtpHeader) {
    super(message);
    setCdtpHeader(cdtpHeader);
  }

  public DispatchException(String message, Throwable cause, CDTPHeader cdtpHeader) {
    super(message, cause);
    setCdtpHeader(cdtpHeader);
  }

  public DispatchException(Throwable cause, CDTPHeader cdtpHeader) {
    super(cause);
    setCdtpHeader(cdtpHeader);
  }

  public DispatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
      CDTPHeader cdtpHeader) {
    super(message, cause, enableSuppression, writableStackTrace);
    setCdtpHeader(cdtpHeader);
  }
}
