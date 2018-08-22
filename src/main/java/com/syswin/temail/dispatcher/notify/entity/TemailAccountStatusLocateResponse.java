package com.syswin.temail.dispatcher.notify.entity;

import java.util.List;
import lombok.Data;

/**
 * Temail状态信息查询结果响应数据 Created by juaihua on 2018/8/14.
 */
@Data
public class TemailAccountStatusLocateResponse {

  private String account;

  private List<TemailAccountStatus> statusList;

}
