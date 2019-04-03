package com.syswin.temail.dispatcher;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping(value = "/healthchk")
  public String healthCheck() {
    return "healthchk is ok";
  }
}
