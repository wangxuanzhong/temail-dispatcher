package com.syswin.temail.cdtp.dispatcher.notify.entity;

import java.util.List;
import lombok.Data;

/**
 * Temail状态信息查询结果响应数据 Created by juaihua on 2018/8/14.
 */
@Data
public class TemailAccountStatusLocateResponse {

  private String account;

  private List<TemailAccountStatus> statusList;

  /**
   * Temail长连接状态信息 Created by juaihua on 2018/8/14.
   */
  @Data
  public static class TemailAccountStatus {

    private String devId;
    private String hostOf;
    private String mqTopic;
  }

}
