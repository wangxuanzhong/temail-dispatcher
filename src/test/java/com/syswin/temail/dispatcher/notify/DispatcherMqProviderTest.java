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
import com.syswin.temail.ps.common.entity.CDTPHeader;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@RunWith(PactRunner.class)
@Provider("temail-dispatcher-mq")
@PactBroker(host = "172.28.50.206", port = "88")
@Ignore
public class DispatcherMqProviderTest {

  private static final String headerJson = "{\n"
      + "  \"header\": {\n"
      + "    \"dataEncryptionMethod\": 0,\n"
      + "    \"receiver\": \"sean@t.email\",\n"
      + "    \"signatureAlgorithm\": 0,\n"
      + "    \"timestamp\": 0\n"
      + "  }\n"
      + "}";
  @TestTarget
  public final Target target = new AmqpTarget(singletonList("com.syswin.temail.dispatcher.notify.*"));
  private final Gson gson = new Gson();

  @State("Notification service is available")
  public void someProviderState() {

  }

  @PactVerifyProvider("online notification")
  public String verifyMessageForOrder() {
    CDTPHeader header = gson.fromJson(headerJson, CDTPHeader.class);

    NotificationMessageFactory factory = new NotificationMessageFactory();

    return factory.notificationOf("sean@t.email", header, gson.toJson(ImmutableMap.of("code", 200, "data", "bonjour")));
  }
}
