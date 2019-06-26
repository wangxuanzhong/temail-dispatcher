/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
public class RemoteChannelStsLocator implements ChannelStsLocator {

  private final ParameterizedTypeReference<Response<TemailAccountLocations>> responseType = new ParameterizedTypeReference<Response<TemailAccountLocations>>() {
  };

  private final HttpEntity<Void> httpEntity;
  private final RestTemplate restTemplate;
  private final String discoveryUrl;

  public RemoteChannelStsLocator(RestTemplate restTemplate, String discoveryUrl) {
    this.restTemplate = restTemplate;
    this.discoveryUrl = discoveryUrl;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);
    this.httpEntity = new HttpEntity<>(headers);
  }

  public List<TemailAccountLocation> locate(String temail) {
    try {
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
            log.info("Succeed to get {} locations: {} .", temail, response);
            return statuses;
          }
        }
      }

      log.error("Failed to get {} locations, url: {}, response status: {}, response body: {} .",
          temail,
          discoveryUrl,
          responseEntity.getStatusCode(),
          response);
    } catch (Exception e) {
      log.error("Exception happened while trying to get user's locations from channel server！", e);
    }

    return Collections.emptyList();
  }
}
