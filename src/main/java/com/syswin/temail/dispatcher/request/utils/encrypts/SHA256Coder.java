package com.syswin.temail.dispatcher.request.utils.encrypts;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class SHA256Coder {

  private MessageDigest messageDigest;

  public SHA256Coder() {
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 组件初始化失败！", e);
    }
  }

  public byte[] encrypt(byte[] data) {
    messageDigest.update(data);
    return messageDigest.digest();
  }

  public String digestWithBase64(byte[] data) {
    return Base64Coder.encrypt(this.encrypt(data));
  }

}