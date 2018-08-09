package com.syswin.temail.cdtp.dispatcher.receive.controller;

import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
@RestController
public class AuthController {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private DispatcherProperties properties;

    @PostMapping(value = "/verify", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(OK)
    public Response<String> verify(MultiValueMap<String, String> body) {
        HttpEntity<?> requestEntity = new HttpEntity<>(body);
        ResponseEntity<Response<String>> responseEntity =
                restTemplate.exchange(properties.getAuthVerifyUrl(), HttpMethod.POST,
                        requestEntity, new ParameterizedTypeReference<Response<String>>() {
                        });
        return responseEntity.getBody();
    }

}
