package com.syswin.temail.dispatcher.request.exceptions;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.Getter;

public class DispatchException extends RuntimeException {

  @Getter
  private final CDTPPacket packet;

  public DispatchException(Throwable throwable, CDTPPacket packet) {
    super(throwable);
    this.packet = packet;
  }

  public DispatchException(String cause, CDTPPacket packet) {
    super(cause);
    this.packet = packet;
  }
}
