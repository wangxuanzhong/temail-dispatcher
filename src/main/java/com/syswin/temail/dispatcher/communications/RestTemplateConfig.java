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

package com.syswin.temail.dispatcher.communications;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class RestTemplateConfig {

  @Value("${app.httpClient.pool.maxTotal}")
  private Integer maxToal;

  @Value("${app.httpClient.pool.defaultMaxPerRoute}")
  private Integer defaultMaxPerRoute;

  @Value("${app.httpClient.pool.connectionRequestTimeout}")
  private Integer connectionRequestTimeout;

  @Value("${app.httpClient.pool.connection.connectTimeout}")
  private Integer connectTimeout;

  @Value("${app.httpClient.pool.connection.readTimeout}")
  private Integer readTimeout;

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(clientHttpRequestFactory());
    restTemplate.setErrorHandler(getResponseErrorHandler());
    return restTemplate;
  }

  public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
    try {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
      PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
      poolingHttpClientConnectionManager.setMaxTotal(maxToal);
      poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
      httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
      httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(1, true));
      HttpClient httpClient = httpClientBuilder.build();

      HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
          httpClient);
      clientHttpRequestFactory.setConnectTimeout(connectTimeout);
      clientHttpRequestFactory.setReadTimeout(readTimeout);
      clientHttpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);

      return clientHttpRequestFactory;

    } catch (Exception e) {
      log.error("Failed to initialize http pool..", e);
      throw e;
    }
  }

  public ResponseErrorHandler getResponseErrorHandler() {

    return new DefaultResponseErrorHandler() {
      @Override
      public boolean hasError(ClientHttpResponse response) throws IOException {
        //always delegate responseEntity to client, no matter the status code is 2xx or not.
        return false;
      }

    };

  }

}