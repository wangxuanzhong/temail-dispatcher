package com.syswin.temail.dispatcher.notify.suspicious;

import lombok.Getter;

public enum RelationType {

  balck(1),
  write(2),
  suspicious(3),
  stranger(4),
  friend(5);

  @Getter
  private int code;

  private RelationType(int code) {
    this.code = code;
  }

}
