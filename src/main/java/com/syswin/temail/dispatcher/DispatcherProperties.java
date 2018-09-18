package com.syswin.temail.dispatcher;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

@Data
@ConfigurationProperties(prefix = "temail.dispatcher")
public class DispatcherProperties {

  private String authVerifyUrl;
  private String temailChannelUrl;
  private RocketMQ rocketmq = new RocketMQ();
  private HttpCliet httpCliet = new HttpCliet();
  private Map<String, Request> cmdMap = new HashMap<>();

  @Data
  public static class RocketMQ {

    private String namesrvAddr;
    private String producerGroup;
    private String consumerGroup;
    private String consumerTopic;
    private String pushTopic = "temail-message";
    private String pushTag = "*";
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
