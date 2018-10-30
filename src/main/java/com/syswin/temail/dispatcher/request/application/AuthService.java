package com.syswin.temail.dispatcher.request.application;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.utils.DigestUtil;
import com.syswin.temail.dispatcher.request.utils.HexUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
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
  static final String PUBLIC_KEY = "publicKey";
  static final String UNSIGNED_BYTES = "UNSIGNED_BYTES";
  static final String SIGNATURE = "SIGNATURE";
  static final String ALGORITHM = "ALGORITHM";
  private final RestTemplate restTemplate;
  private final String authUrl;
  private final String specialAuthUrl;
  private final HttpHeaders headers = new HttpHeaders();
  private final ParameterizedTypeReference<Response<String>> responseType = responseType();

  public AuthService(RestTemplate restTemplate, String authUrl) {
    this.restTemplate = restTemplate;
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

  public ResponseEntity<Response<String>> verify(CDTPPacketTrans packet) {
    CDTPHeader header = packet.getHeader();
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (CommandAwarePacketUtil.isSendSingleMsg(commandSpace, command) || CommandAwarePacketUtil
        .isGroupJoin(commandSpace, command)) {
      return verifyRecieverTemail(header.getSender(), header.getSenderPK(), extractUnsignedData(packet),
          header.getSignature(), String.valueOf(header.getSignatureAlgorithm()));
    } else {
      return verify(header.getSender(), extractUnsignedData(packet),
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
    log.debug("{}, {}, {}, {} 验签结果： {} ", temail, unsignedBytes, signature, algorithm, result.getStatusCode());
    return result;
  }

  private String extractUnsignedData(CDTPPacketTrans packet) {
    CDTPHeader header = packet.getHeader();
    String targetAddress = defaultString(header.getTargetAddress());
    String data = packet.getData();
    String dataSha256 = data == null ? "" :
        HexUtil.encodeHex(
            DigestUtil.sha256(
                CommandAwarePacketUtil.decodeData(packet, true)));

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
