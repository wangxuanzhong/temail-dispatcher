package com.syswin.temail.cdtp.dispatcher.request.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CDTPPackage extends CDTPHeader {

  private String data;

  public CDTPPackage() {
  }

  public CDTPPackage(CDTPHeader header) {
    super(header);
  }
}
