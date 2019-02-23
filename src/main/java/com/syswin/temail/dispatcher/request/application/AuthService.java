package com.syswin.temail.dispatcher.request.application;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class AuthService {
  static final ResponseEntity<Response<String>> ALWAYS_SUCCESS =
      new ResponseEntity<Response<String>>(Response.ok(HttpStatus.OK,"Success"),HttpStatus.OK);
  static final String TE_MAIL = "TeMail";
  static final String PUBLIC_KEY = "publicKey";
  static final String UNSIGNED_BYTES = "UNSIGNED_BYTES";
  static final String SIGNATURE = "SIGNATURE";
  static final String ALGORITHM = "ALGORITHM";
  private final RestTemplate restTemplate;
  private final String authUrl;
  private final String specialAuthUrl;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();
  private final CommandAwarePacketUtil packetUtil;

  public AuthService(RestTemplate restTemplate, String authUrl,
      CommandAwarePacketUtil packetUtil) {
    this.restTemplate = restTemplate;
    this.packetUtil = packetUtil;
    if (authUrl.endsWith("verify")) {
      // 对老配置做一个兼容
      this.authUrl = authUrl;
      this.specialAuthUrl = authUrl + "RecieverTemail";
    } else {
      if (!authUrl.endsWith("/")) {
        authUrl += "/";
      }
      this.authUrl = authUrl + "verify";
      this.specialAuthUrl = authUrl + "verifyRecieverTemail";
    }
    this.headers.setContentType(APPLICATION_FORM_URLENCODED);
  }

  public ResponseEntity<Response<String>> verify(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if(packetUtil.isGroupType(commandSpace)){
      return ALWAYS_SUCCESS;
    }else if (packetUtil.isToBeVerifyRecieverTemail(commandSpace, command)) {
      return verifyRecieverTemail(header.getReceiver(), header.getSenderPK(), packetUtil.extractUnsignedData(packet),
          header.getSignature(), String.valueOf(header.getSignatureAlgorithm()));
    } else {
      return verify(header.getSender(), packetUtil.extractUnsignedData(packet),
          header.getSignature(), String.valueOf(header.getSignatureAlgorithm()));
    }
  }

  public ResponseEntity<Response<String>> verifyRecieverTemail(String temail, String publicKey, String unsignedBytes,
      String signature, String algorithm) {
    return verify(specialAuthUrl, temail, publicKey, unsignedBytes, signature, algorithm);
  }

  public ResponseEntity<Response<String>> verify(String temail, String unsignedBytes,
      String signature, String algorithm) {
    return verify(authUrl, temail, null, unsignedBytes, signature, algorithm);
  }

  private ResponseEntity<Response<String>> verify(String authUrl, String temail, String publicKey, String unsignedBytes,
      String signature, String algorithm) {
    MultiValueMap<String, String> entityBody = new LinkedMultiValueMap<>();
    entityBody.add(TE_MAIL, temail);
    if (publicKey != null) {
      entityBody.add(PUBLIC_KEY, publicKey);
    }
    entityBody.add(UNSIGNED_BYTES, unsignedBytes);
    entityBody.add(SIGNATURE, signature);
    entityBody.add(ALGORITHM, algorithm);
    HttpEntity<?> requestEntity = new HttpEntity<>(entityBody, headers);
    ResponseEntity<Response<String>> result = restTemplate.exchange(authUrl, POST, requestEntity, responseType);
    log.debug("{}, {}, {}, {} signature verify result ： {} ", temail, unsignedBytes, signature, algorithm, result.getStatusCode());
    return result;
  }

  private ParameterizedTypeReference<Response<String>> responseType() {
    return new ParameterizedTypeReference<Response<String>>() {
    };
  }
}
