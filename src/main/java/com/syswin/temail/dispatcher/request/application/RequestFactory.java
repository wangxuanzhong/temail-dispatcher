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
public class RequestFactory {

  public static final String UNSUPPORTED_CMD_PREfIX = "unsupported command type";
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

  public RequestFactory(DispatcherProperties properties, CommandAwarePacketUtil packetUtil) {
    this.properties = properties;
    this.packetUtil = packetUtil;
  }

  public TemailRequest toRequest(CDTPPacket packet) {
    int combinedCommand = (packet.getCommandSpace() << 16) + packet.getCommand();
    String cmdHex = Integer.toHexString(combinedCommand).toUpperCase();
    Request request = properties.getCmdMap().get(cmdHex);
    if (request == null) {
      log.error(UNSUPPORTED_CMD_PREfIX + "：{}, request param：{}", cmdHex, packet);
      throw new DispatchException(UNSUPPORTED_CMD_PREfIX + "：" + combinedCommand, packet);
    }

    CDTPParams params = packetUtil.buildParams(packet);
    if (packetUtil.isBizServerValidType(packet.getCommandSpace()) || packetUtil.isCrowMsg(packet.getCommandSpace(),packet.getCommand())) {
      Map<String, String> requestHeaders = new HashMap<>();
      requestHeaders.put(TE_MAIL, packet.getHeader().getSender());
      requestHeaders.put(PUBLIC_KEY, packet.getHeader().getSenderPK());
      requestHeaders.put(UNSIGNED_BYTES, packetUtil.extractUnsignedData(packet));
      requestHeaders.put(SIGNATURE, packet.getHeader().getSignature());
      requestHeaders.put(ALGORITHM, String.valueOf(packet.getHeader().getSignatureAlgorithm()));
      if (params.getHeader() == null) {
        params.setHeader(requestHeaders);
      } else {
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
    log.info("dispatch request info ：URL={}, method={}, entity={}", url, request.getMethod(),
        entity);
    return new TemailRequest(url, request.getMethod(), entity);
  }

  private HttpEntity<Map<String, Object>> composeHttpEntity(Request request, CDTPHeader cdtpHeader,
      CDTPParams params) {
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

  private String composeUrl(Request request, Map<String, Object> path,
      Map<String, String> queries) {
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
