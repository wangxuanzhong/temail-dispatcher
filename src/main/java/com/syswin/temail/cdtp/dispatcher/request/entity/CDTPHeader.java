package com.syswin.temail.cdtp.dispatcher.request.entity;

import com.google.gson.Gson;
import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-14
 */

@Data
public class CDTPHeader {

  private int command;
  private int version;
  private int algorithm;
  private String sign;
  private int dem;
  private long timestamp;
  private String pkgId;
  private String from;
  private String to;
  private String senderPK;
  private String receiverPK;

  public CDTPHeader() {
  }

  public CDTPHeader(CDTPHeader header) {
    if (header == null) {
      return;
    }
    this.setCommand(header.getCommand());
    this.setVersion(header.getVersion());
    this.setAlgorithm(header.getAlgorithm());
    this.setSign(header.getSign());
    this.setDem(header.getDem());
    this.setTimestamp(header.getTimestamp());
    this.setPkgId(header.getPkgId());
    this.setFrom(header.getFrom());
    this.setTo(header.getTo());
    this.setSenderPK(header.getSenderPK());
    this.setReceiverPK(header.getReceiverPK());
  }

}
