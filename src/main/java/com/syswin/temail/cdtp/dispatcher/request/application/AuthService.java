package com.syswin.temail.cdtp.dispatcher.request.application;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

  private final RestTemplate restTemplate;
  private final String authUrl;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();

  @Autowired
  public AuthService(RestTemplate restTemplate, String authUrl) {
    this.authUrl = authUrl;
    this.restTemplate = restTemplate;

    this.headers.setContentType(APPLICATION_FORM_URLENCODED);
  }

  public ResponseEntity<Response<String>> verify(MultiValueMap<String, String> body) {
    HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

    return restTemplate.exchange(authUrl, POST, requestEntity, responseType);
  }

  private ParameterizedTypeReference<Response<String>> responseType() {
    return new ParameterizedTypeReference<Response<String>>() {
    };
  }
}
