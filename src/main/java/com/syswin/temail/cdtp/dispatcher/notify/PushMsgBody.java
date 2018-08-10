package com.syswin.temail.cdtp.dispatcher.notify;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
@Data
public class PushMsgBody {
    private String toTemail;
    private String data;
}
