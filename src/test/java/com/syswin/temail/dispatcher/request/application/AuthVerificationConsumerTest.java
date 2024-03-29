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

package com.syswin.temail.dispatcher.request.application;

import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syswin.temail.dispatcher.request.controller.Response;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.web.client.RestTemplate;

@Ignore
public class AuthVerificationConsumerTest extends ConsumerPactTestMk2 {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestTemplate restTemplate = new RestTemplate();

  @Before
  public void setUp() {
    restTemplate.setErrorHandler(new SilentResponseErrorHandler());
  }

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> reqHeaders = singletonMap(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
    Map<String, String> respHeaders = singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);
//    Map<String, String> respHeaders = new HashMap<>();
//    respHeaders.put(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);

    try {
      // Verify
      PactDslResponse response = pactDslWithProvider
          .given("Verify - User Mike exists")
          .uponReceiving("request for user Mike")
          .method("POST")
          .headers(reqHeaders)
          .body("TeMail=mike%40t.email&UNSIGNED_BYTES=abc&SIGNATURE=xyz&ALGORITHM=2")
          .path("/verify")
          .willRespondWith()
          .status(OK.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.ok(OK, "Success")))

          .given("Verify - User Jane does not exist")
          .uponReceiving("request for user Jane")
          .method("POST")
          .headers(reqHeaders)
          .body("TeMail=jane%40t.email&UNSIGNED_BYTES=abc&SIGNATURE=xyz&ALGORITHM=2")
          .path("/verify")
          .willRespondWith()
          .status(NOT_FOUND.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.failed(NOT_FOUND, "No such user exists: jane@t.email")))

          .given("Verify - User Mike exists and signature is error")
          .uponReceiving("User Mike exists and signature is error")
          .method("POST")
          .body("TeMail=mike%40t.email&UNSIGNED_BYTES=abc&SIGNATURE=xyzxyz&ALGORITHM=2")
          .headers(reqHeaders)
          .path("/verify")
          .willRespondWith()
          .status(FORBIDDEN.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.ok(FORBIDDEN, "Signature is error")))

          .given("Verify - UnsupportedAlgorithm")
          .uponReceiving("UnsupportedAlgorithm")
          .method("POST")
          .body("TeMail=mike%40t.email&UNSIGNED_BYTES=abc&SIGNATURE=xyz&ALGORITHM=5")
          .headers(reqHeaders)
          .path("/verify")
          .willRespondWith()
          .status(BAD_REQUEST.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.failed(BAD_REQUEST, "Unsupported Algorithm: 5")))

          .given("Verify - Invalid request")
          .uponReceiving("request without signature")
          .method("POST")
          .headers(reqHeaders)
          .path("/verify")
          .willRespondWith()
          .status(BAD_REQUEST.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.failed(BAD_REQUEST,
              "temail or signature or UNSIGNED_BYTES is null")))

          // verifyRecieverTemail
          .given("VerifyRecieverTemail - User Mike exists")
          .uponReceiving("request for user Mike")
          .method("POST")
          .body("TeMail=mike%40t.email&publicKey=pk&UNSIGNED_BYTES=abc&SIGNATURE=xyz&ALGORITHM=2")
          .headers(reqHeaders)
          .path("/verifyRecieverTemail")
          .willRespondWith()
          .status(OK.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.ok(OK, "Success")))

          .given("VerifyRecieverTemail - User Jane does not exist")
          .uponReceiving("request for user Jane")
          .method("POST")
          .body("TeMail=jane%40t.email&publicKey=pk&UNSIGNED_BYTES=abc&SIGNATURE=xyz&ALGORITHM=2")
          .headers(reqHeaders)
          .path("/verifyRecieverTemail")
          .willRespondWith()
          .status(NOT_FOUND.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.failed(NOT_FOUND, "No such user exists: jane@t.email")))

          .given("VerifyRecieverTemail - User Mike exists and signature is error")
          .uponReceiving("User Mike exists and signature is error")
          .method("POST")
          .body("TeMail=mike%40t.email&publicKey=pk&UNSIGNED_BYTES=abc&SIGNATURE=xyzxyz&ALGORITHM=2")
          .headers(reqHeaders)
          .path("/verifyRecieverTemail")
          .willRespondWith()
          .status(FORBIDDEN.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.ok(FORBIDDEN, "Signature is error")))

          .given("VerifyRecieverTemail - UnsupportedAlgorithm")
          .uponReceiving("Unsupported Algorithm")
          .method("POST")
          .body("TeMail=mike%40t.email&publicKey=pk&UNSIGNED_BYTES=abc&SIGNATURE=xyz&ALGORITHM=5")
          .headers(reqHeaders)
          .path("/verifyRecieverTemail")
          .willRespondWith()
          .status(BAD_REQUEST.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.failed(BAD_REQUEST, "Unsupported Algorithm: 5")))

          .given("VerifyRecieverTemail - Invalid request")
          .uponReceiving("request without signature")
          .method("POST")
          .body("TeMail=&publicKey=&UNSIGNED_BYTES=&SIGNATURE=&ALGORITHM=")
          .headers(reqHeaders)
          .path("/verifyRecieverTemail")
          .willRespondWith()
          .status(BAD_REQUEST.value())
          .headers(respHeaders)
          .body(objectMapper.writeValueAsString(Response.failed(BAD_REQUEST,
              "temail or publicKey or signature or UNSIGNED_BYTES is null")));

      return response.toPact();
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void runTest(MockServer mockServer) {
    //String url = mockServer.getUrl();
    //PacketTypeJudge packetTypeJudge = new PacketTypeJudge(null);
    //CommandAwarePacketUtil packetUtil = new CommandAwarePacketUtil(packetTypeJudge);
    //DispAuthService authService = new DispAuthService(restTemplate, url, packetUtil,packetTypeJudge);
    //
    //// verify
    //ResponseEntity<Response<String>> response;
    //response = authService.verify("mike@t.email", "abc", "xyz", "2");
    //assertThat(response.getStatusCode()).isEqualTo(OK);
    //
    //response = authService.verify("jane@t.email", "abc", "xyz", "2");
    //assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    //
    //response = authService.verify("mike@t.email", "abc", "xyzxyz", "2");
    //assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    //
    //response = authService.verify("mike@t.email", "abc", "xyz", "5");
    //assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    //
    //response = authService.verify(null, null, null, null);
    //assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    //
    //// verifyRecieverTemail
    //response = authService.verifyRecieverTemail("mike@t.email", "pk", "abc", "xyz", "2");
    //assertThat(response.getStatusCode()).isEqualTo(OK);
    //
    //response = authService.verifyRecieverTemail("jane@t.email", "pk", "abc", "xyz", "2");
    //assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    //
    //response = authService.verifyRecieverTemail("mike@t.email", "pk", "abc", "xyzxyz", "2");
    //assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    //
    //response = authService.verifyRecieverTemail("mike@t.email", "pk", "abc", "xyz", "5");
    //assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    //
    //response = authService.verifyRecieverTemail("", "", "", "", "");
    //assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Override
  protected String providerName() {
    return "temail-auth";
  }

  @Override
  protected String consumerName() {
    return "temail-dispatcher";
  }
}
