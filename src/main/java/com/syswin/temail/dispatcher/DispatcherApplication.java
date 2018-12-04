package com.syswin.temail.dispatcher;

import com.systoon.ocm.framework.swagger.EnableSwagger2Doc;
import com.syswin.temail.dispatcher.codec.CommandAwarePredicate;
import com.syswin.temail.dispatcher.codec.PacketTypeJudger;
import com.syswin.temail.dispatcher.codec.RawPacketDecoder;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.application.CommandAwarePacketUtil;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import java.util.function.BiPredicate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableSwagger2Doc
@SpringBootApplication
@EnableConfigurationProperties({DispatcherProperties.class})
public class DispatcherApplication {

  public static void main(String[] args) {
    SpringApplication.run(DispatcherApplication.class, args);
  }

  @Bean
  PacketTypeJudger packetTypeJudger(DispatcherProperties properties) {
    return new PacketTypeJudger(properties);
  }

  @Bean
  BiPredicate<Short, Short> commandAwarePredicate(PacketTypeJudger packetTypeJudger) {
    return new CommandAwarePredicate(packetTypeJudger);
  }

  @Bean
  RawPacketDecoder packetDecoder(BiPredicate<Short, Short> predicate) {
    return new RawPacketDecoder(predicate);
  }

  @Bean
  public CommandAwarePacketUtil packetUtil(PacketTypeJudger packetTypeJudger) {
    return new CommandAwarePacketUtil(packetTypeJudger);
  }

  @Bean
  public PackageDispatcher packageDispatcher(DispatcherProperties properties, RestTemplate restTemplate,
      CommandAwarePacketUtil packetUtil) {
    return new PackageDispatcher(properties, restTemplate, packetUtil);
  }

  @Bean
  public AuthService authService(DispatcherProperties properties, RestTemplate restTemplate,
      CommandAwarePacketUtil packetUtil) {
    return new AuthService(restTemplate, properties.getAuthVerifyUrl(), packetUtil);
  }
}
