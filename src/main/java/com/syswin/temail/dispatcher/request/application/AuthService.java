package com.syswin.temail.dispatcher.request.application;

import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import com.syswin.temail.dispatcher.request.utils.CommonBizUtils;
import com.syswin.temail.dispatcher.request.utils.encrypts.SHA256Coder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Slf4j
public class AuthService {

  private final RestTemplate restTemplate;
  private SHA256Coder sha256Coder = new SHA256Coder();
  private final String authUrl;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();

  static final String TE_MAIL = "TeMail";
  static final String UNSIGNED_BYTES = "UNSIGNED_BYTES";
  static final String SIGNATURE = "SIGNATURE";
  static final String ALGORITHM = "ALGORITHM";

  public AuthService(RestTemplate restTemplate, String authUrl) {
    this.authUrl = authUrl;
    this.restTemplate = restTemplate;
    this.headers.setContentType(APPLICATION_FORM_URLENCODED);
  }

  public ResponseEntity<Response<String>> verify(CDTPPacketTrans packetTrans) {
    return verify(packetTrans.getHeader().getSender(), extractUnsignedData(packetTrans),
        packetTrans.getHeader().getSignature(), String.valueOf(packetTrans.getHeader().getSignatureAlgorithm()));
  }

  public ResponseEntity<Response<String>> verify(String temail, String unsignedBytes,
      String signature, String algorithm) {
    MultiValueMap<String, String> entityBody = new LinkedMultiValueMap<>();
    entityBody.add(TE_MAIL, temail);
    entityBody.add(UNSIGNED_BYTES, unsignedBytes);
    entityBody.add(SIGNATURE, signature);
    entityBody.add(ALGORITHM, algorithm);
    HttpEntity<?> requestEntity = new HttpEntity<>(entityBody, headers);
    ResponseEntity<Response<String>> result =  restTemplate.exchange(authUrl, POST, requestEntity, responseType);
    log.info("{}, {}, {}, {} 验签结果： {} ", temail, unsignedBytes, signature, algorithm, result.getStatusCode());
    return result;
  }

  public String extractUnsignedData(CDTPPacketTrans cdtpPacketTrans) {
    StringBuilder unSignedData = new StringBuilder();
    unSignedData.append((cdtpPacketTrans.getCommandSpace() + cdtpPacketTrans.getCommand()))
        .append(cdtpPacketTrans.getHeader().getReceiver())
        .append(cdtpPacketTrans.getHeader().getTimestamp())
        .append(sha256Coder.encryptAndSwitch2Base64(CommonBizUtils.decodeData(cdtpPacketTrans)));
    return unSignedData.toString();
  }

  private ParameterizedTypeReference<Response<String>> responseType() {
    return new ParameterizedTypeReference<Response<String>>() {};
  }

}
