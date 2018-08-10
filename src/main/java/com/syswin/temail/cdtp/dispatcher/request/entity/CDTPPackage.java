package com.syswin.temail.cdtp.dispatcher.request.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CDTPPackage extends CDTPHeader {
    private CDTPBody data;
}
