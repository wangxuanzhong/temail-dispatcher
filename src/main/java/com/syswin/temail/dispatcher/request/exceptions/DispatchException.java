package com.syswin.temail.dispatcher.request.exceptions;

import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 姚华成
 * @date 2018-8-15
 */
public class DispatchException extends RuntimeException {

  @Getter
  @Setter
  private CDTPPacketTrans packet;

  public DispatchException(CDTPPacketTrans packet) {
    setPacket(packet);
  }

  public DispatchException(String message, CDTPPacketTrans packet) {
    super(message);
    setPacket(packet);
  }

  public DispatchException(String message, Throwable cause, CDTPPacketTrans packet) {
    super(message, cause);
    setPacket(packet);
  }

  public DispatchException(Throwable cause, CDTPPacketTrans packet) {
    super(cause);
    setPacket(packet);
  }

  public DispatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
      CDTPPacketTrans packet) {
    super(message, cause, enableSuppression, writableStackTrace);
    setPacket(packet);
  }
}
