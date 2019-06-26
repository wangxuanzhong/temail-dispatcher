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
