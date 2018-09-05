package com.syswin.temail.dispatcher.request.application;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans.Header;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import java.util.HashMap;
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
  private Gson gson = new Gson();
  private DispatcherProperties properties;

  RequestFactory(DispatcherProperties properties) {
    this.properties = properties;
  }

  TemailRequest toRequest(CDTPPacketTrans packet) {
    CDTPParams params;
    Gson gson = new Gson();
    try {
      if (isSendSingleMsg(packet)) {
        params = buildSendSingleMsgParams(packet);
      } else {
        params = gson.fromJson(packet.getData(), CDTPParams.class);
      }
    } catch (JsonSyntaxException e) {
      log.error("请求参数：{}" + packet);
      throw new DispatchException(e, packet);
    }
    int command = (packet.getCommandSpace() << 16) + packet.getCommand();
    Request request = properties.getCmdMap().get(Integer.toHexString(command).toUpperCase());

    if (request == null) {
      log.error("不支持的命令类型：{}, 请求参数：{}", Integer.toHexString(command), packet);
      throw new RuntimeException("不支持的命令类型：" + command);
    }

    HttpEntity<?> entity = composeHttpEntity(request, packet.getHeader(), params);
    if (entity == null) {
      log.error("请求参数：{}", packet);
      throw new RuntimeException("不支持的命令类型：" + request.getMethod());
    }

    String url = composeUrl(request, params.getQuery());
    log.info("转发的请求：URL={}, method={}, entity={}", url, request.getMethod(), entity);
    return new TemailRequest(url, request.getMethod(), entity);
  }


  private HttpEntity<?> composeHttpEntity(Request request, CDTPPacketTrans.Header cdtpHeader, CDTPParams params) {
    MultiValueMap<String, String> headers = addHeaders(cdtpHeader, params);

    switch (request.getMethod()) {
      case GET:
        return new HttpEntity<>(headers);
      case DELETE:
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

  private String composeUrl(Request request, Map<String, String> queries) {
    String url = request.getUrl();
    if (queries != null && !queries.isEmpty()) {
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
      for (Entry<String, String> entry : queries.entrySet()) {
        builder.queryParam(entry.getKey(), entry.getValue());
      }
      url = builder.toUriString();
    }
    return url;
  }

  private MultiValueMap<String, String> addHeaders(Header cdtpHeader, CDTPParams params) {
    Map<String, String> paramsHeaders = params.getHeader();
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    if (paramsHeaders != null && !paramsHeaders.isEmpty()) {
      for (Entry<String, String> entry : paramsHeaders.entrySet()) {
        headers.add(entry.getKey(), entry.getValue());
      }
    }
    headers.add(CDTP_HEADER, gson.toJson(cdtpHeader));
    return headers;
  }

  private boolean isSendSingleMsg(CDTPPacketTrans packet) {
    // TODO(姚华成): 根据业务定义，可能会改变
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    return commandSpace == 1 && command == 1;
  }

  private CDTPParams buildSendSingleMsgParams(CDTPPacketTrans packet) {
    Header header = packet.getHeader();
    Map<String, Object> extraData = gson
        .fromJson(header.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());
    Map<String, Object> body = new HashMap<>(extraData);
    body.put("sender", header.getSender());
    body.put("receiver", header.getReceiver());
    body.put("msgData", packet.getData());

    return new CDTPParams(body);
  }

}
