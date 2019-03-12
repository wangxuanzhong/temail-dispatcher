package com.syswin.temail.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.dispatcher")
public class DispatcherProperties {

  private String authBaseUrl;

  private String temailChannelUrl;

  private RocketMQ rocketmq = new RocketMQ();

  private Map<String, Request> cmdMap = new HashMap<>();

  private Map<String, List<String>> validStrategy = new HashMap<>();

  @Data
  @ConfigurationProperties(prefix = "app.dispatcher.rocketmq")
  public static class RocketMQ {
    private String namesrvAddr;
    private String producerGroup;
    private String consumerGroup;
    private String consumerTopic;
    private String pushTopic = "";
    private String pushTag = "";
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {
    private String url;
    private HttpMethod method;
  }

}
