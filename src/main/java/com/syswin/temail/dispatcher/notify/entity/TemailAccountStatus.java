package com.syswin.temail.dispatcher.notify.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by juaihua on 2018/8/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemailAccountStatus {

  private String devId;

  private String hostOf;

  private String processId;

  private String mqTopic;

  private String mqTag;

  public String geneHashKey() {
    return this.devId + "-" + this.hostOf + "-"
        + Optional.ofNullable(this.processId).orElse("|")
        + "-" + mqTopic + "-" + mqTag;
  }

}
