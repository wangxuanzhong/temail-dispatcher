package com.syswin.temail.cdtp.dispatcher.request.application;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import com.syswin.temail.cdtp.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.cdtp.dispatcher.request.exceptions.TeMailUnsupportedCommandException;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody.CDTPParams;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
class RequestFactory {

  static final String CDTP_HEADER = "CDTP-header";
  static final Gson BODY_EXCLUSIVE_GSON = new GsonBuilder()
      .setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
          return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
          return CDTPBody.class == aClass;
        }
      })
      .create();

  private final DispatcherProperties properties;

  RequestFactory(DispatcherProperties properties) {
    this.properties = properties;
  }

  TeMailRequest toRequest(CDTPPackage cdtpPackage) {
    CDTPBody cdtpBody = cdtpPackage.getData();
    CDTPParams params = cdtpBody.getParams();
    Request request = properties.getCmdRequestMap().get(cdtpBody.getCommand());

    if (request == null) {
      log.error("不支持的命令类型：{}, 请求参数：{}", cdtpBody.getCommand(), cdtpPackage);
      throw new TeMailUnsupportedCommandException("不支持的命令类型：" + cdtpBody.getCommand());
    }
    HttpEntity<?> entity = composeRequestBody(cdtpPackage, params, request);
    String url = composeUrl(request, params.getQuery());

    return new TeMailRequest(url, request.getMethod(), entity);
  }

  private MultiValueMap<String, String> addHeaders(CDTPPackage cdtpPackage, CDTPParams params) {
    Map<String, List<String>> paramsHeaders = params.getHeader();
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    if (paramsHeaders != null && !paramsHeaders.isEmpty()) {
      for (Entry<String, List<String>> entry : paramsHeaders.entrySet()) {
        headers.addAll(entry.getKey(), entry.getValue());
      }
    }
    headers.add(CDTP_HEADER, BODY_EXCLUSIVE_GSON.toJson(cdtpPackage));
    return headers;
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

  private HttpEntity<?> composeRequestBody(CDTPPackage cdtpPackage,
      CDTPParams params,
      Request request) {

    MultiValueMap<String, String> headers = addHeaders(cdtpPackage, params);

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
        log.error("请求参数：{}", cdtpPackage);
        throw new TeMailUnsupportedCommandException("不支持的命令类型：" + request.getMethod());
    }
  }
}
