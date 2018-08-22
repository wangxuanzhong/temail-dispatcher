package com.syswin.temail.cdtp.dispatcher;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

@Data
@ConfigurationProperties(prefix = "cdtp")
public class DispatcherProperties {

  private String authVerifyUrl;
  private String cdtpStatusUrl;
  private Map<Integer, Request> cmdMap = new HashMap<>();
  private RocketMQ rocketmq = new RocketMQ();
  private HttpCliet httpCliet = new HttpCliet();

  @Data
  public static class RocketMQ {

    private String namesrvAddr;
    private String producerGroup;
    private String producerTopic;
    private String consumerGroup;
    private String consumerTopic;
  }

  @Data
  public static class Request {

    private String url;
    private HttpMethod method;
  }

  @Data
  public static class HttpCliet {

    private int readTimeoutInMilli = 3000;
    private int connectTimeoutInMilli = 3000;
  }
}
