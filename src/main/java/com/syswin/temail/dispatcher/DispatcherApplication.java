package com.syswin.temail.dispatcher;

import com.systoon.ocm.framework.swagger.EnableSwagger2Doc;
import com.syswin.temail.dispatcher.codec.CommandAwarePredicate;
import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.notify.NotificationMessageFactory;
import com.syswin.temail.dispatcher.notify.suspicious.SuspiciousExtractTaskRunner;
import com.syswin.temail.dispatcher.notify.suspicious.TaskExecutor;
import com.syswin.temail.dispatcher.request.application.CommandAwarePacketUtil;
import com.syswin.temail.dispatcher.request.application.DispAuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.application.RequestFactory;
import com.syswin.temail.dispatcher.request.service.DispDispatcherService;
import com.syswin.temail.dispatcher.request.service.DispDispatcherServiceImpl;
import com.syswin.temail.dispatcher.valid.PacketValidJudge;
import java.util.function.BiPredicate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableSwagger2Doc
@SpringBootApplication
public class DispatcherApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(DispatcherApplication.class, args);
  }

  @Bean
  public NotificationMessageFactory geneNotificationMessageFactory() {
    return new NotificationMessageFactory();
  }

  @Bean
  public TaskExecutor geneTaskExecutor(DispatcherProperties properties,RestTemplate restTemplat,
      NotificationMessageFactory notificationMessageFactory) {
    return new SuspiciousExtractTaskRunner(restTemplat, notificationMessageFactory, properties, t->{});
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
  public PackageDispatcher packageDispatcher(DispatcherProperties properties,
      RestTemplate restTemplate, CommandAwarePacketUtil packetUtil) {
    return new PackageDispatcher(properties, restTemplate,
        new RequestFactory(properties,packetUtil));
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
  public DispDispatcherService getDispatService(PackageDispatcher packageDispatcher, DispAuthService dispAuthService,
      DispRawPacketDecoder dispRawPacketDecoder) {
    return new DispDispatcherServiceImpl(packageDispatcher, dispAuthService, dispRawPacketDecoder);
  }

}
