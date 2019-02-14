package com.syswin.temail.dispatcher.request.application;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.DispatcherProperties.Request;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
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
  static final String X_PACKET_ID = "X-PACKET-ID";

  static final String TE_MAIL = "TE-MAIL";
  static final String PUBLIC_KEY = "PUBLIC-KEY";
  static final String UNSIGNED_BYTES = "UNSIGNED-BYTES";
  static final String SIGNATURE = "SIGNATURE";
  static final String ALGORITHM = "ALGORITHM";

  private Gson gson = new Gson();
  private DispatcherProperties properties;
  private CommandAwarePacketUtil packetUtil;

  RequestFactory(DispatcherProperties properties, CommandAwarePacketUtil packetUtil) {
    this.properties = properties;
    this.packetUtil = packetUtil;
  }

  TemailRequest toRequest(CDTPPacket packet) {
    int combinedCommand = (packet.getCommandSpace() << 16) + packet.getCommand();
    String cmdHex = Integer.toHexString(combinedCommand).toUpperCase();
    Request request = properties.getCmdMap().get(cmdHex);
    if (request == null) {
      log.error("unsupported command type：{}, request param：{}", cmdHex, packet);
      throw new DispatchException("unsupported command type：" + combinedCommand, packet);
    }

    CDTPParams params = packetUtil.buildParams(packet);
    if(packetUtil.isGroupType(packet.getCommandSpace())){
      Map<String, String> requestHeaders = new HashMap<>();
      requestHeaders.put(TE_MAIL, packet.getHeader().getSender());
      requestHeaders.put(PUBLIC_KEY, packet.getHeader().getSenderPK());
      requestHeaders.put(UNSIGNED_BYTES, packetUtil.extractUnsignedData(packet));
      requestHeaders.put(SIGNATURE, packet.getHeader().getSignature());
      requestHeaders.put(ALGORITHM, String.valueOf(packet.getHeader().getSignatureAlgorithm()));
      if(params.getHeader() == null){
        params.setHeader(requestHeaders);
      }else {
        //in case that the params.header is unmodifable.
        requestHeaders.putAll(params.getHeader());
        params.setHeader(requestHeaders);
      }
    }

    HttpEntity<Map<String, Object>> entity = composeHttpEntity(request, packet.getHeader(), params);
    if (entity == null) {
      log.error("unsupported request type：{}, request param：{}", request.getMethod(), packet);
      throw new DispatchException("unsupported request type：" + request.getMethod(), packet);
    }


    String url = composeUrl(request, params.getPath(), params.getQuery());
    log.info("dispatch request info ：URL={}, method={}, entity={}", url, request.getMethod(), entity);
    return new TemailRequest(url, request.getMethod(), entity);
  }

  private HttpEntity<Map<String, Object>> composeHttpEntity(Request request, CDTPHeader cdtpHeader, CDTPParams params) {
    MultiValueMap<String, String> headers = addHeaders(cdtpHeader, params);

    switch (request.getMethod()) {
      case GET:
        return new HttpEntity<>(headers);
      case DELETE:
      case POST:
      case PUT:
        Map<String, Object> body = params.getBody();
        if (body != null) {
          if (!headers.containsKey(CONTENT_TYPE)) {
            headers.add(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);
          }
          return new HttpEntity<>(body, headers);
        }
        return new HttpEntity<>(headers);
      default:
        return null;
    }
  }

  private String composeUrl(Request request, Map<String, Object> path, Map<String, String> queries) {
    String url = request.getUrl();
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    if (queries != null && !queries.isEmpty()) {
      for (Entry<String, String> entry : queries.entrySet()) {
        builder.queryParam(entry.getKey(), entry.getValue());
      }
    }

    if (path != null) {
      builder.uriVariables(path);
    }
    url = builder.toUriString();
    return url;
  }

  private MultiValueMap<String, String> addHeaders(CDTPHeader cdtpHeader, CDTPParams params) {
    Map<String, String> paramsHeaders = params.getHeader();
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    if (paramsHeaders != null && !paramsHeaders.isEmpty()) {
      for (Entry<String, String> entry : paramsHeaders.entrySet()) {
        headers.add(entry.getKey(), entry.getValue());
      }
    }
    headers.add(CDTP_HEADER, gson.toJson(cdtpHeader));
    headers.add(X_PACKET_ID, cdtpHeader.getPacketId());
    return headers;
  }

}
