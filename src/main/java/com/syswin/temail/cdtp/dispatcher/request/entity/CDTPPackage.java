package com.syswin.temail.cdtp.dispatcher.request.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CDTPPackage extends CDTPHeader {

  private String data;

  public CDTPPackage(CDTPHeader header) {
    super(header);
  }


}
