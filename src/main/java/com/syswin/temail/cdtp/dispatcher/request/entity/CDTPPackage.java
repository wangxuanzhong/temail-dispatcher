package com.syswin.temail.cdtp.dispatcher.request.entity;

import lombok.Data;

@Data
public class CDTPPackage<T> {
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
    private T data;
}
