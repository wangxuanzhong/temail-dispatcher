package com.syswin.temail.dispatcher;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 姚华成
 * @date 2018-04-23
 */
@RestController
public class HealthController {

  @GetMapping(value = "/healthchk")
  public String healthCheck() {
    return "healthchk is ok";
  }
}
