package com.syswin.temail.cdtp.dispatcher.request.application;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import com.syswin.temail.cdtp.dispatcher.request.entity.AuthData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class AuthService {

  private static final String TE_MAIL = "TeMail";
  private static final String UNSIGNED_BYTES = "UNSIGNED_BYTES";
  private static final String SIGNATURE = "SIGNATURE";
  private final RestTemplate restTemplate;
  private final String authUrl;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();

  public AuthService(RestTemplate restTemplate, String authUrl) {
    this.authUrl = authUrl;
    this.restTemplate = restTemplate;

    this.headers.setContentType(APPLICATION_FORM_URLENCODED);
  }

  public ResponseEntity<Response<String>> verify(AuthData body) {
    MultiValueMap<String, String> entityBody = new LinkedMultiValueMap<>();
    entityBody.add(TE_MAIL, body.getTemail());
    entityBody.add(UNSIGNED_BYTES, body.getUnsignedBytes());
    entityBody.add(SIGNATURE, body.getSignature());

    HttpEntity<?> requestEntity = new HttpEntity<>(entityBody, headers);

    return restTemplate.exchange(authUrl, POST, requestEntity, responseType);
  }

  private ParameterizedTypeReference<Response<String>> responseType() {
    return new ParameterizedTypeReference<Response<String>>() {
    };
  }
}
