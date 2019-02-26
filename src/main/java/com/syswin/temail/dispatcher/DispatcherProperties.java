package com.syswin.temail.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "app.dispatcher")
public class DispatcherProperties {

  //TODO delete after new valid process is ok
  private String authVerifyUrl;

  private String authBaseUrl;

  private String temailChannelUrl;

  @Autowired
  private RocketMQ rocketmq;

  private Map<String, Request> cmdMap = new HashMap<>();

  private Map<String, List<String>> validStrategy = new HashMap<>();

  @Data
  @Component
  @ConfigurationProperties(prefix = "spring.rocketmq")
  public static class RocketMQ {

    private String namesrvAddr;

    private String producerGroup;

    private String consumerGroup;

    private String consumerTopic;

    private String pushTopic = "";

    private String pushTag = "";

  }

  @Data
  public static class Request {
    private String url;
    private HttpMethod method;
  }

}
