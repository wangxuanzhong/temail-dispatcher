package com.syswin.temail.dispatcher.request.service;

import com.syswin.temail.dispatcher.request.controller.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface DispDispatcherService {

  public ResponseEntity<Response<String>> verify(@RequestBody byte[] payload) throws Exception ;

  public ResponseEntity<String> dispatch(@RequestBody byte[] payload) throws Exception ;

}
