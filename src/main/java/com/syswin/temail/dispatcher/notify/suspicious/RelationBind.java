package com.syswin.temail.dispatcher.notify.suspicious;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelationBind {

  private String receiver;

  private String sender;

  private int contactType;

}
