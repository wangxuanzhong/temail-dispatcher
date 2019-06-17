package com.syswin.temail.dispatcher.request.service;

import com.syswin.temail.dispatcher.request.controller.Response;
import org.springframework.http.ResponseEntity;

public interface DispDispatcherService {

  ResponseEntity<Response<String>> verify( byte[] payload) throws Exception ;

  ResponseEntity<String> dispatch( byte[] payload) throws Exception ;

}
