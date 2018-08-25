package com.syswin.temail.dispatcher.request.exceptions;

import com.syswin.temail.dispatcher.request.entity.CDTPPacket;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 姚华成
 * @date 2018-8-15
 */
public class DispatchException extends RuntimeException {

  @Getter
  @Setter
  private CDTPPacket packet;

  public DispatchException(CDTPPacket packet) {
    setPacket(packet);
  }

  public DispatchException(String message, CDTPPacket packet) {
    super(message);
    setPacket(packet);
  }

  public DispatchException(String message, Throwable cause, CDTPPacket packet) {
    super(message, cause);
    setPacket(packet);
  }

  public DispatchException(Throwable cause, CDTPPacket packet) {
    super(cause);
    setPacket(packet);
  }

  public DispatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
      CDTPPacket packet) {
    super(message, cause, enableSuppression, writableStackTrace);
    setPacket(packet);
  }
}
