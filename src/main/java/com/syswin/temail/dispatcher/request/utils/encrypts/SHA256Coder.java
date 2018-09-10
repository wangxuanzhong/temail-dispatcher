package com.syswin.temail.dispatcher.request.utils.encrypts;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Coder {

  private MessageDigest messageDigest;

  public SHA256Coder(){
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  public byte[] encrypt(byte[] data){
    messageDigest.update(data);
    return messageDigest.digest();
  }

  public String encryptAndSwitch2Base64(byte[] data){
    return Base64Coder.encrypt(this.encrypt(data));
  }

}