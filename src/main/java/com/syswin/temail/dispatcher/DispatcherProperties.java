/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

  private String relationBaseUrl;

  private String temailChannelUrl;

  private String mockUrl;

  private RocketMQ rocketmq = new RocketMQ();

  private Map<String, Request> cmdMap = new HashMap<>();

  private Map<String, List<String>> validStrategy = new HashMap<>();

  private String offPushType;

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
