package com.syswin.temail.dispatcher.request.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CDTPPackage extends CDTPHeader {

  private String data;

  public CDTPPackage() {
  }

  public CDTPPackage(CDTPHeader header) {
    super(header);
  }
}
