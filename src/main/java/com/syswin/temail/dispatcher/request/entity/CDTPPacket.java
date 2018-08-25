package com.syswin.temail.dispatcher.request.entity;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Data
public final class CDTPPacket {

  private short commandSpace;
  private short command;
  private short version;
  private Header header;
  private byte[] data;

  @Data
  public static final class Header {

    private String deviceId;
    private int signatureAlgorithm;
    private String signature;
    private int dataEncryptionMethod;
    private long timestamp;
    private String packetId;
    private String sender;
    private String receiver;
    private String senderPK;
    private String receiverPK;
    private String at;
    private String topic;
    private String extraData;
  }

}
