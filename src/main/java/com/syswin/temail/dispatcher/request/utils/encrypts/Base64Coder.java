package com.syswin.temail.dispatcher.request.utils.encrypts;


import java.util.Base64;

public class Base64Coder {

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  public static String encrypt(byte[] data){
    return ENCODER.encodeToString(data);
  }

  public static byte[] decrypt(String base64Str) throws Exception{
    return DECODER.decode(base64Str);
  }

  public static String hexString(byte[] data){
    StringBuilder sbd = new StringBuilder();
    for(int i = 0; i < data.length; i++){
      String hexStr = Integer.toHexString((0xFF & data[i]));
      if(hexStr.length() == 1){hexStr = "0"+hexStr;}
      sbd.append(hexStr);
    }
    return sbd.toString();
  }

}
