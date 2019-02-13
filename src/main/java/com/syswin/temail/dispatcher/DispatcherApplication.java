package com.syswin.temail.dispatcher;

import com.systoon.ocm.framework.swagger.EnableSwagger2Doc;
import com.syswin.temail.dispatcher.codec.CommandAwarePredicate;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
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
  PacketTypeJudge packetTypeJudge() {
    return new PacketTypeJudge();
  }

  @Bean
  BiPredicate<Short, Short> commandAwarePredicate(PacketTypeJudge packetTypeJudge) {
    return new CommandAwarePredicate(packetTypeJudge);
  }

  @Bean
  RawPacketDecoder packetDecoder(BiPredicate<Short, Short> predicate) {
    return new RawPacketDecoder(predicate);
  }

  @Bean
  public CommandAwarePacketUtil packetUtil(PacketTypeJudge packetTypeJudge) {
    return new CommandAwarePacketUtil(packetTypeJudge);
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
