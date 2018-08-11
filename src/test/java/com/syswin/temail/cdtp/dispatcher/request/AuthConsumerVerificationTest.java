package com.syswin.temail.cdtp.dispatcher.request;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syswin.temail.cdtp.dispatcher.request.application.AuthService;
import com.syswin.temail.cdtp.dispatcher.request.application.Response;
import com.syswin.temail.cdtp.dispatcher.request.application.SilentResponseErrorHandler;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class AuthConsumerVerificationTest extends ConsumerPactTestMk2 {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestTemplate restTemplate = new RestTemplate();

  @Before
  public void setUp() {
    restTemplate.setErrorHandler(new SilentResponseErrorHandler());
  }

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);

    try {
      return pactDslWithProvider
          .given("Verify - User Mike exists")
            .uponReceiving("request for user Mike")
            .method("POST")
            .body("TeMail=mike%40t.email&UNSIGNED_BYTES=abc&SIGNATURE=xyz")
            .headers(headers)
            .path("/verify")
            .willRespondWith()
            .status(200)
            .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .body(objectMapper.writeValueAsString(Response.ok(OK, "Success")))
          .given("Verify - User Jane does not exist")
            .uponReceiving("request for user Jane")
            .method("POST")
            .body("TeMail=jane%40t.email&UNSIGNED_BYTES=abc&SIGNATURE=xyz")
            .headers(headers)
            .path("/verify")
            .willRespondWith()
            .status(FORBIDDEN.value())
            .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .body(objectMapper.writeValueAsString(Response.failed(FORBIDDEN, "No such user exists: jane@t.email")))
          .given("Verify - Invalid request")
            .uponReceiving("request without signature")
            .method("POST")
            .headers(headers)
            .path("/verify")
            .willRespondWith()
            .status(400)
            .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .body(objectMapper.writeValueAsString(Response.failed(BAD_REQUEST, "TeMail address or public key is invalid")))
          .toPact();
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + "/verify";
    AuthService authService = new AuthService(restTemplate, url);

    ResponseEntity<Response<String>> response = authService.verify(httpEntityOf("mike@t.email", "abc", "xyz"));
    assertThat(response.getStatusCode()).isEqualTo(OK);

    response = authService.verify(httpEntityOf("jane@t.email", "abc", "xyz"));
    assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);

    response = authService.verify(null);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Override
  protected String providerName() {
    return "temail-auth";
  }

  @Override
  protected String consumerName() {
    return "temail-gateway";
  }

  private MultiValueMap<String, String> httpEntityOf(String email, String unsigned, String signature) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("TeMail", email);
    map.add("UNSIGNED_BYTES", unsigned);
    map.add("SIGNATURE", signature);

    return map;
  }
}
