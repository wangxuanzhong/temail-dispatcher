package com.syswin.temail.cdtp.dispatcher.request.entity;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-10
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
}
