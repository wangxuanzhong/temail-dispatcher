package com.syswin.temail.dispatcher;

import com.systoon.ocm.framework.swagger.EnableSwagger2Doc;
import com.syswin.temail.dispatcher.codec.CommandAwarePredicate;
import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.request.application.DispAuthService;
import com.syswin.temail.dispatcher.request.application.CommandAwarePacketUtil;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.service.DispatcherService;
import com.syswin.temail.dispatcher.request.service.DispatcherServiceImpl;
import com.syswin.temail.dispatcher.valid.PacketValidJudge;
import java.util.function.BiPredicate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableSwagger2Doc
@SpringBootApplication
public class DispatcherApplication {

  public static void main(String[] args) {
    SpringApplication.run(DispatcherApplication.class, args);
  }

  @Bean
  PacketTypeJudge packetTypeJudge(DispatcherProperties properties) {
    return new PacketTypeJudge(properties);
  }

  @Bean
  BiPredicate<Short, Short> commandAwarePredicate(PacketTypeJudge packetTypeJudge) {
    return new CommandAwarePredicate(packetTypeJudge);
  }

  @Bean
  DispRawPacketDecoder packetDecoder(BiPredicate<Short, Short> predicate) {
    return new DispRawPacketDecoder(predicate);
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
  public PacketValidJudge packetValidJudge(DispatcherProperties properties) {
    return new PacketValidJudge(properties);
  }

  @Bean
  public DispAuthService authService(DispatcherProperties properties, RestTemplate restTemplate,
      CommandAwarePacketUtil packetUtil, PacketValidJudge packetValidJudge) {
    return new DispAuthService(restTemplate, properties, packetUtil, packetValidJudge);
  }

  @Bean
  public DispatcherService getDispatService(PackageDispatcher packageDispatcher, DispAuthService dispAuthService,
      DispRawPacketDecoder dispRawPacketDecoder) {
    return new DispatcherServiceImpl(packageDispatcher, dispAuthService, dispRawPacketDecoder);
  }
}
