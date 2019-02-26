package com.syswin.temail.dispatcher.valid.params;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ValidParams {

  private String authUri;

  private Map<String, String> params;

  public boolean isAnValidParam() {
    return this.params != null && !this.params.isEmpty()
        && !StringUtils.isEmpty(this.authUri);
  }

}
