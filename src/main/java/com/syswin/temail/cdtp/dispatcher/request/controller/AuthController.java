package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    @PostMapping(value = "/verify", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(OK)
    public Response<String> verify(MultiValueMap<String, String> body) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Response<String>> responseEntity =
                restTemplate.exchange(properties.getAuthVerifyUrl(), HttpMethod.POST,
                        requestEntity, new ParameterizedTypeReference<Response<String>>() {
                        });
        return responseEntity.getBody();
    }

}
