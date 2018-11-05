package com.syswin.temail.dispatcher;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "app.dispatcher")
public class DispatcherProperties {
  // TODO 这个选项是为了在客户端未完成时，把代码更新到服务器而不影响客户端的功能使用。功能正式上线后选项要删除。
  private boolean groupPacketEnabled = false;

  private String authVerifyUrl;
  private String temailChannelUrl;
  @Autowired
  private RocketMQ rocketmq;
  private HttpCliet httpCliet = new HttpCliet();
  private Map<String, Request> cmdMap = new HashMap<>();

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

  @Data
  public static class HttpCliet {

    private int readTimeoutInMilli = 3000;
    private int connectTimeoutInMilli = 3000;
  }
}
