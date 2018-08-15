package com.syswin.temail.cdtp.dispatcher.request.application;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPHeader;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPParams;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class PackageDispatcher {

  private final RestTemplate restTemplate;
  private Gson gson = new GsonBuilder()
      .setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
          return fieldAttributes.getName().equals("data");
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
          return false;
        }
      })
      .create();
  private DispatcherProperties properties;


  public PackageDispatcher(DispatcherProperties properties, RestTemplate restTemplate) {
    this.properties = properties;
    this.restTemplate = restTemplate;
  }

  public ResponseEntity<String> dispatch(CDTPPackage cdtpPackage) {

    CDTPParams params = new Gson().fromJson(cdtpPackage.getData(), CDTPParams.class);
    int command = cdtpPackage.getCommand();
    Request request = properties.getCmdRequestMap().get(command);

    if (request == null) {
      log.error("不支持的命令类型：{}, 请求参数：{}", command, cdtpPackage);
      throw new RuntimeException("不支持的命令类型：" + command);
    }

    HttpEntity<?> entity = composeHttpEntity(request, cdtpPackage, params);
    if (entity == null) {
      log.error("请求参数：{}", cdtpPackage);
      throw new RuntimeException("不支持的命令类型：" + request.getMethod());
    }

    String url = composeUrl(request, params.getQuery());

    return restTemplate.exchange(url, request.getMethod(), entity, String.class);
  }


  private HttpEntity<?> composeHttpEntity(Request request, CDTPHeader cdtpHeader, CDTPParams params) {
    MultiValueMap<String, String> headers = addHeaders(cdtpHeader, params);

    switch (request.getMethod()) {
      case GET:
      case DELETE:
        return new HttpEntity<>(headers);
      case POST:
      case PUT:
        Map<String, Object> body = params.getBody();
        if (body != null) {
          headers.add(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);
          return new HttpEntity<>(body, headers);
        }
        return new HttpEntity<>(headers);
      default:
        return null;
    }
  }

  private String composeUrl(Request request, Map<String, List<String>> queries) {
    String url = request.getUrl();
    if (queries != null && !queries.isEmpty()) {
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
      for (Entry<String, List<String>> entry : queries.entrySet()) {
        builder.queryParam(entry.getKey(), entry.getValue().toArray());
      }
      url = builder.toUriString();
    }
    return url;
  }

  private MultiValueMap<String, String> addHeaders(CDTPHeader cdtpHeader, CDTPParams params) {
    Map<String, List<String>> paramsHeaders = params.getHeader();
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    if (paramsHeaders != null && !paramsHeaders.isEmpty()) {
      for (Entry<String, List<String>> entry : paramsHeaders.entrySet()) {
        headers.addAll(entry.getKey(), entry.getValue());
      }
    }
    String CDTP_HEADER = "CDTP-header";
    headers.add(CDTP_HEADER, gson.toJson(cdtpHeader));
    return headers;
  }

}
