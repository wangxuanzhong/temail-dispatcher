package com.syswin.temail.dispatcher.request;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import com.syswin.temail.dispatcher.request.application.AuthService;
import com.syswin.temail.dispatcher.request.controller.Response;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

@RunWith(SpringRestPactRunner.class)
@PactBroker(host = "172.28.50.206", port = "88")
@Provider("temail-dispatcher")
@SpringBootTest(webEnvironment = DEFINED_PORT, properties = "server.port=8081")
@ActiveProfiles("dev")
public class LoginProviderTest {
  @TestTarget
  public final Target target = new HttpTarget(8081);
  private final String unsignedBytes = "abc";
  private final String signature = "signed-abc";

  @MockBean
  private AuthService authService;

  @State("User sean is registered")
  public void userIsRegistered() {
    when(authService.verify("sean@t.email", unsignedBytes, signature)).thenReturn(ResponseEntity.ok(Response.ok("Success")));
  }

  @State("User jack is not registered")
  public void userNotRegistered() {
    when(authService.verify("jack@t.email", unsignedBytes, signature)).thenReturn(new ResponseEntity<>(Response.failed(FORBIDDEN), FORBIDDEN));
  }

  @State("User mike is registered, but server is out of work")
  public void serverOutOfWork() {
    when(authService.verify("mike@t.email", unsignedBytes, signature)).thenThrow(RestClientException.class);
  }
}
