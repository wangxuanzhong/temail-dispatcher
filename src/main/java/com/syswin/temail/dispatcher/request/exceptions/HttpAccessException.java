package com.syswin.temail.dispatcher.request.exceptions;

import com.syswin.temail.dispatcher.request.application.TemailRequest;
import lombok.Data;

@Data
public class HttpAccessException extends RuntimeException {

  private TemailRequest temailRequest;

  public HttpAccessException(Throwable throwable, TemailRequest temailRequest){
    super(throwable);
    this.temailRequest = temailRequest;
  }


}
