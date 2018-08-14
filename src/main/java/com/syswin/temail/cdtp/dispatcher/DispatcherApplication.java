package com.syswin.temail.cdtp.dispatcher;

import com.syswin.temail.cdtp.dispatcher.request.application.AuthService;
import com.syswin.temail.cdtp.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.cdtp.dispatcher.request.application.SilentResponseErrorHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//@EnableSwagger2Doc
@SpringBootApplication
@EnableConfigurationProperties({DispatcherProperties.class})
public class DispatcherApplication {

  public static void main(String[] args) {
    SpringApplication.run(DispatcherApplication.class, args);
  }

  @Bean
  RestTemplate restTemplate(DispatcherProperties properties) {
    return new RestTemplateBuilder()
        .setConnectTimeout(properties.getHttpCliet().getConnectTimeoutInMilli())
        .setReadTimeout(properties.getHttpCliet().getReadTimeoutInMilli())
        .errorHandler(new SilentResponseErrorHandler())
        .build();
  }

  @Bean
  PackageDispatcher responseFetcher(DispatcherProperties properties, RestTemplate restTemplate) {
    return new PackageDispatcher(properties, restTemplate);
  }

  @Bean
  AuthService authService(DispatcherProperties properties, RestTemplate restTemplate) {
    return new AuthService(restTemplate, properties.getAuthVerifyUrl());
  }
}
