package com.syswin.temail.dispatcher.request.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Data
@EqualsAndHashCode
@ToString
public final class CDTPPacketTrans {

  private short commandSpace;
  private short command;
  private short version;
  private Header header;
  private String data;

  @Data
  @EqualsAndHashCode
  @ToString
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
