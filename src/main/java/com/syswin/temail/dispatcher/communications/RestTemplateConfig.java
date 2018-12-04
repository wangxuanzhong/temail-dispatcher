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