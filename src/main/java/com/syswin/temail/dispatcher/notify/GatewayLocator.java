package com.syswin.temail.dispatcher.notify;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocations;
import com.syswin.temail.dispatcher.request.controller.Response;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
class GatewayLocator {

  private final ParameterizedTypeReference<Response<TemailAccountLocations>> responseType = new ParameterizedTypeReference<Response<TemailAccountLocations>>() {
  };

  private final HttpEntity<Void> httpEntity;
  private final RestTemplate restTemplate;
  private final String discoveryUrl;

  GatewayLocator(RestTemplate restTemplate, String discoveryUrl) {
    this.restTemplate = restTemplate;
    this.discoveryUrl = discoveryUrl;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);
    this.httpEntity = new HttpEntity<>(headers);
  }

  List<TemailAccountLocation> locate(String temail) {
    try {
      log.debug("try to get user's channel locations from channel server. url={}, temail={}", discoveryUrl, temail);

      ResponseEntity<Response<TemailAccountLocations>> responseEntity = restTemplate.exchange(
          discoveryUrl,
          GET,
          httpEntity,
          responseType,
          temail);

      Response<TemailAccountLocations> response = responseEntity.getBody();
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        if (response != null) {
          List<TemailAccountLocation> statuses = response.getData().getStatuses();
          if (statuses != null) {
            log.debug("success to get user's channel locations from channel server. url = {}, temail = {}, response = {}", discoveryUrl, temail, response);
            return statuses;
          }
        }
      }

      log.error("fail to get user's channel locations from channel server: url = {}, temail = {}, response status = {}, response body = {}",
          discoveryUrl,
          temail,
          responseEntity.getStatusCode(),
          response);
    } catch (Exception e) {
      log.error("exception happened while trying to get user's locations from channel server！", e);
    }

    return Collections.emptyList();
  }
}
