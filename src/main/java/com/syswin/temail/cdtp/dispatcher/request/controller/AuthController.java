package com.syswin.temail.cdtp.dispatcher.request.controller;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.syswin.temail.cdtp.dispatcher.request.application.AuthService;
import com.syswin.temail.cdtp.dispatcher.request.application.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/verify", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Response<String>> verify(MultiValueMap<String, String> body) {
        return authService.verify(body);
    }

}
