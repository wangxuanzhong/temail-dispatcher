package com.syswin.temail.dispatcher.request.application;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans.Header;
import com.syswin.temail.dispatcher.request.utils.CommonPacketDecode;
import com.syswin.temail.dispatcher.request.utils.DigestUtil;
import com.syswin.temail.dispatcher.request.utils.PacketDecode;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class AuthService {

  static final String TE_MAIL = "TeMail";
  static final String UNSIGNED_BYTES = "UNSIGNED_BYTES";
  static final String SIGNATURE = "SIGNATURE";
  static final String ALGORITHM = "ALGORITHM";
  private final RestTemplate restTemplate;
  private final PacketDecode packetDecode;
  private final String authUrl;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();

  public AuthService(RestTemplate restTemplate, String authUrl) {
    this.authUrl = authUrl;
    this.restTemplate = restTemplate;
    this.headers.setContentType(APPLICATION_FORM_URLENCODED);
    this.packetDecode = new CommonPacketDecode();
  }

  public ResponseEntity<Response<String>> verify(CDTPPacketTrans packetTrans) {
    Header header = packetTrans.getHeader();
    return verify(header.getSender(), extractUnsignedData(packetTrans),
        header.getSignature(), String.valueOf(header.getSignatureAlgorithm()));
  }

  public ResponseEntity<Response<String>> verify(String temail, String unsignedBytes,
      String signature, String algorithm) {
    MultiValueMap<String, String> entityBody = new LinkedMultiValueMap<>();
    entityBody.add(TE_MAIL, temail);
    entityBody.add(UNSIGNED_BYTES, unsignedBytes);
    entityBody.add(SIGNATURE, signature);
    entityBody.add(ALGORITHM, algorithm);
    HttpEntity<?> requestEntity = new HttpEntity<>(entityBody, headers);
    ResponseEntity<Response<String>> result = restTemplate.exchange(authUrl, POST, requestEntity, responseType);
    log.debug("{}, {}, {}, {} 验签结果： {} ", temail, unsignedBytes, signature, algorithm, result.getStatusCode());
    return result;
  }

  private String extractUnsignedData(CDTPPacketTrans packet) {
    Header header = packet.getHeader();
    String targetAddress = defaultString(header.getTargetAddress());
    String data = packet.getData();
    String dataSha256 = data == null ? "" :
        Base64.getUrlEncoder().encodeToString(
            DigestUtil.sha256(
                packetDecode.decodeData(packet)));

    return String.valueOf(packet.getCommandSpace() + packet.getCommand())
        + targetAddress
        + String.valueOf(header.getTimestamp())
        + dataSha256;
  }

  private ParameterizedTypeReference<Response<String>> responseType() {
    return new ParameterizedTypeReference<Response<String>>() {
    };
  }

}
