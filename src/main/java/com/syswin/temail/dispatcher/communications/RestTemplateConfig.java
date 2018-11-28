package com.syswin.temail.dispatcher.communications;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class RestTemplateConfig {

  @Value("${httpClient.pool.maxTotal}")
  private Integer maxToal;

  @Value("${httpClient.pool.defaultMaxPerRoute}")
  private Integer defaultMaxPerRoute;

  @Value("${httpClient.pool.waitConnection}")
  private Integer waitConnection;

  @Value("${httpClient.pool.connection.connectTimeout}")
  private Integer connectTimeout;

  @Value("${httpClient.pool.connection.readTimeout}")
  private Integer readTimeout;

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(clientHttpRequestFactory());
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
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

      HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
      clientHttpRequestFactory.setConnectTimeout(connectTimeout);
      clientHttpRequestFactory.setReadTimeout(readTimeout);
      clientHttpRequestFactory.setConnectionRequestTimeout(waitConnection);

      return clientHttpRequestFactory;

    } catch (Exception e) {
      log.error("failed to initlize http pool..", e);
      throw new IllegalStateException("http connection pool can not be initialized!",e);
    }
  }
}