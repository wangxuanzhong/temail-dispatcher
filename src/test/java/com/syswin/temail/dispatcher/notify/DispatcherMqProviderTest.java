package com.syswin.temail.dispatcher.notify;

import static java.util.Collections.singletonList;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import org.junit.runner.RunWith;

@RunWith(PactRunner.class)
@Provider("temail-dispatcher-mq")
@PactBroker(host = "172.28.50.206", port = "88")
public class DispatcherMqProviderTest {

  @TestTarget
  public final Target target = new AmqpTarget(singletonList("com.syswin.temail.dispatcher.notify.*"));

  private static final String headerJson = "{\n"
      + "  \"header\": {\n"
      + "    \"dataEncryptionMethod\": 0,\n"
      + "    \"receiver\": \"sean@t.email\",\n"
      + "    \"signatureAlgorithm\": 0,\n"
      + "    \"timestamp\": 0\n"
      + "  }\n"
      + "}";

  private final Gson gson = new Gson();

  @State("Notification service is available")
  public void someProviderState() {

  }

  @PactVerifyProvider("online notification")
  public String verifyMessageForOrder() {
    CDTPPacketTrans.Header header = gson.fromJson(headerJson, CDTPPacketTrans.Header.class);

    NotificationMessageFactory factory = new NotificationMessageFactory();

    return factory.notificationOf("sean@t.email", header, ImmutableMap.of("code", 200, "data", "bonjour"));
  }
}
