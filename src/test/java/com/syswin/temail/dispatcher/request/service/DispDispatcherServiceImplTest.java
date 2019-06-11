package com.syswin.temail.dispatcher.request.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.dispatcher.request.application.DispAuthService;
import com.syswin.temail.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.dispatcher.request.application.RequestFactory;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.dispatcher.request.exceptions.DispatchException;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class DispDispatcherServiceImplTest {

  private static final Gson gson = new Gson();

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(
      WireMockConfiguration.wireMockConfig().dynamicPort());

  private final RestTemplate restTemplate = new RestTemplate();
  private final RequestFactory requestFactory = mock(RequestFactory.class);
  private final DispatchException dispatchException = mock(DispatchException.class);
  private final DispatcherProperties dispatcherProperties = mock(DispatcherProperties.class);
  private final ResponseEntity success = new ResponseEntity(HttpStatus.OK);
  private final ResponseEntity failer = new ResponseEntity(HttpStatus.BAD_REQUEST);
  private final PacketEncoder packetEncoder = new PacketEncoder();

  private final DispAuthService dispAuthService = mock(DispAuthService.class);
  private final DispRawPacketDecoder dispRawPacketDecoder = mock(DispRawPacketDecoder.class);
  private final CDTPPacket cdtpPacket = PacketMaker.loginPacket("sean@syswin.com", "deviceId");
  private PackageDispatcher packageDispatcher = new PackageDispatcher(dispatcherProperties,
      restTemplate, requestFactory);

  private DispDispatcherServiceImpl dispDispatcherService = new DispDispatcherServiceImpl(
      packageDispatcher, dispAuthService, dispRawPacketDecoder);

  private final byte[] data = new byte[]{0, 0, 6, 1, 0, 1, 0, 1, 0, 1, 3, 117, 10, 17, 109, 97, 105,
      108, 45, 103, 97, 116, 101, 119, 97, 121, 45, 104, 111, 115, 116, 16, 2, 26, -70, 1, 77, 73,
      71, 73, 65, 107, 73, 66, 102, 55, 106, 69, 51, 70, 101, 85, 110, 116, 66, 50, 69, 87, 112,
      105, 55, 65, 100, 84, 101, 78, 73, 102, 50, 102, 120, 99, 71, 69, 89, 66, 119, 45, 121, 77,
      73, 112, 116, 70, 95, 88, 100, 54, 98, 116, 115, 100, 108, 112, 77, 76, 68, 102, 108, 53, 57,
      71, 119, 114, 66, 98, 54, 103, 86, 120, 112, 104, 97, 68, 68, 83, 108, 112, 118, 116, 89, 122,
      120, 50, 107, 65, 52, 45, 52, 98, 77, 67, 81, 103, 71, 72, 121, 90, 97, 102, 107, 75, 107, 77,
      119, 72, 85, 48, 70, 50, 56, 79, 52, 45, 49, 89, 80, 95, 69, 48, 108, 116, 77, 106, 122, 89,
      69, 75, 97, 107, 45, 88, 45, 72, 100, 120, 76, 101, 57, 71, 75, 65, 48, 103, 102, 88, 107,
      108, 88, 119, 118, 85, 107, 97, 118, 84, 55, 101, 83, 75, 114, 70, 76, 57, 85, 73, 71, 108,
      76, 45, 111, 84, 66, 119, 51, 56, 70, 55, 80, 108, 73, 81, 32, 4, 41, -34, 64, -50, 44, 107,
      1, 0, 0, 50, 36, 48, 53, 50, 57, 101, 54, 101, 98, 45, 56, 97, 101, 57, 45, 52, 98, 98, 51,
      45, 57, 57, 51, 50, 45, 99, 56, 51, 57, 48, 98, 98, 101, 53, 49, 55, 97, 58, 24, 109, 97, 105,
      108, 95, 103, 97, 116, 101, 119, 97, 121, 64, 109, 115, 103, 115, 101, 97, 108, 46, 99, 111,
      109, 66, -45, 1, 77, 73, 71, 98, 77, 66, 65, 71, 66, 121, 113, 71, 83, 77, 52, 57, 65, 103,
      69, 71, 66, 83, 117, 66, 66, 65, 65, 106, 65, 52, 71, 71, 65, 65, 81, 65, 103, 114, 50, 75,
      53, 78, 70, 116, 109, 49, 71, 122, 122, 76, 51, 53, 86, 55, 98, 122, 74, 68, 115, 86, 57, 86,
      56, 119, 113, 89, 103, 81, 118, 88, 45, 80, 88, 109, 72, 104, 120, 57, 121, 119, 71, 107, 115,
      99, 56, 80, 115, 111, 65, 115, 112, 103, 77, 121, 84, 52, 101, 105, 101, 105, 89, 95, 88, 99,
      99, 102, 72, 57, 90, 49, 105, 112, 116, 103, 45, 74, 103, 45, 97, 119, 75, 82, 73, 65, 122,
      81, 86, 76, 77, 121, 89, 88, 95, 117, 70, 66, 115, 122, 108, 106, 56, 90, 99, 117, 97, 88, 85,
      118, 104, 122, 77, 72, 68, 106, 112, 45, 90, 53, 49, 116, 112, 121, 79, 45, 77, 98, 112, 83,
      98, 57, 113, 99, 72, 109, 99, 57, 107, 68, 112, 112, 83, 102, 72, 80, 107, 78, 95, 98, 98,
      114, 114, 81, 77, 81, 103, 117, 70, 79, 78, 98, 77, 118, 80, 110, 79, 48, 69, 109, 74, 106,
      119, 74, 22, 112, 101, 110, 103, 109, 97, 110, 115, 104, 97, 110, 64, 115, 121, 115, 119, 105,
      110, 46, 99, 111, 109, 82, -45, 1, 77, 73, 71, 98, 77, 66, 65, 71, 66, 121, 113, 71, 83, 77,
      52, 57, 65, 103, 69, 71, 66, 83, 117, 66, 66, 65, 65, 106, 65, 52, 71, 71, 65, 65, 81, 65, 45,
      116, 82, 77, 113, 100, 75, 100, 117, 74, 102, 90, 103, 121, 68, 115, 97, 88, 68, 50, 110, 115,
      122, 55, 99, 71, 105, 104, 79, 109, 75, 53, 45, 79, 45, 76, 48, 100, 119, 71, 56, 121, 77,
      119, 45, 79, 78, 53, 48, 77, 57, 88, 67, 107, 112, 121, 110, 121, 100, 84, 108, 48, 87, 101,
      113, 70, 115, 87, 102, 66, 50, 98, 66, 57, 95, 82, 83, 70, 88, 81, 50, 82, 82, 72, 99, 49, 48,
      66, 117, 57, 108, 107, 113, 74, 117, 76, 70, 99, 87, 50, 98, 85, 87, 69, 74, 74, 103, 72, 70,
      57, 50, 49, 52, 100, 110, 109, 122, 56, 86, 87, 77, 108, 83, 84, 115, 69, 113, 85, 111, 116,
      84, 108, 115, 98, 107, 89, 55, 76, 77, 70, 120, 100, 83, 113, 45, 83, 122, 104, 114, 85, 53,
      78, 101, 83, 115, 79, 106, 65, 54, 67, 77, 77, 88, 95, 104, 118, 69, 66, 66, 70, 120, 102, 53,
      98, 85, 106, -111, 1, 123, 34, 102, 114, 111, 109, 34, 58, 34, 101, 115, 95, 97, 108, 101,
      114, 116, 64, 115, 121, 115, 119, 105, 110, 46, 99, 111, 109, 34, 44, 34, 109, 115, 103, 73,
      100, 34, 58, 34, 48, 50, 55, 57, 102, 57, 54, 99, 45, 52, 101, 54, 49, 45, 52, 55, 99, 97, 45,
      98, 50, 102, 53, 45, 57, 100, 52, 48, 51, 101, 97, 56, 54, 57, 97, 56, 34, 44, 34, 115, 116,
      111, 114, 101, 84, 121, 112, 101, 34, 58, 49, 44, 34, 115, 117, 115, 112, 105, 99, 105, 111,
      117, 115, 34, 58, 48, 44, 34, 116, 111, 34, 58, 34, 112, 101, 110, 103, 109, 97, 110, 115,
      104, 97, 110, 64, 115, 121, 115, 119, 105, 110, 46, 99, 111, 109, 34, 44, 34, 116, 121, 112,
      101, 34, 58, 48, 125, 65, 65, 65, 65, 81, 119, 65, 65, 65, 69, 65, 65, 65, 65, 70, 81, 65, 65,
      65, 66, 83, 65, 77, 65, 79, 111, 114, 53, 112, 114, 48, 50, 76, 80, 110, 49, 85, 56, 114, 65,
      84, 95, 90, 68, 103, 100, 113, 100, 45, 78, 79, 121, 54, 81, 115, 67, 114, 122, 107, 48, 69,
      54, 104, 102, 87, 69, 88, 109, 82, 76, 118, 76, 115, 48, 85, 70, 106, 122, 75, 110, 101, 97,
      112, 80, 87, 104, 70, 82, 87, 77, 74, 82, 97, 55, 103, 120, 79, 72, 86, 114, 79, 78, 56, 86,
      69, 85, 116, 106, 118, 67, 85, 70, 84, 100, 113, 54, 51, 45, 65, 81, 114, 114, 104, 73, 70,
      104, 120, 65, 122, 85, 84, 118, 88, 121, 55, 105, 102, 98, 119, 106, 72, 75, 121, 53, 108,
      105, 115, 76, 77, 117, 101, 99, 113, 81, 53, 75, 105, 116, 112, 105, 111, 99, 95, 101, 89,
      113, 114, 95, 121, 82, 90, 85, 72, 114, 106, 45, 116, 112, 95, 56, 69, 100, 103, 73, 114, 68,
      48, 110, 49, 78, 52, 120, 81, 115, 111, 52, 57, 54, 57, 76, 108, 119, 116, 99, 116, 108, 71,
      100, 97, 117, 120, 45, 81, 87, 87, 103, 73, 107, 81, 109, 115, 68, 69, 55, 81, 99, 90, 101,
      119, 77, 54, 120, 101, 101, 106, 85, 69, 90, 73, 114, 98, 109, 82, 75, 109, 65, 90, 95, 69,
      119, 104, 45, 111, 67, 74, 76, 51, 71, 108, 109, 119, 57, 102, 76, 65, 110, 80, 45, 121, 66,
      117, 110, 51, 68, 88, 68, 105, 48, 56, 82, 107, 80, 98, 95, 109, 45, 55, 104, 71, 90, 95, 78,
      109, 122, 87, 108, 80, 116, 66, 45, 115, 106, 97, 78, 75, 84, 87, 76, 113, 117, 88, 121, 53,
      67, 52, 95, 112, 108, 76, 74, 117, 55, 111, 65, 51, 68, 80, 75, 115, 83, 90, 56, 119, 90, 76,
      110, 75, 66, 98, 110, 99, 116, 80, 70, 51, 57, 95, 50, 109, 87, 86, 80, 113, 121, 65, 100,
      102, 100, 114, 88, 104, 51, 108, 104, 108, 102, 97, 109, 83, 116, 97, 111, 109, 52, 66, 69,
      101, 75, 121, 111, 49, 102, 111, 115, 105, 83, 45, 121, 89, 122, 48, 67, 77, 49, 97, 114, 89,
      72, 109, 83, 99, 76, 120, 56, 116, 82, 79, 75, 107, 97, 83, 117, 75, 55, 102, 52, 45, 112, 68,
      85, 67, 45, 106, 65, 102, 122, 78, 103, 87, 106, 57, 86, 83, 54, 84, 121, 119, 113, 66, 99,
      79, 76, 112, 117, 70, 78, 68, 52, 85, 69, 97, 99, 113, 54, 55, 48, 102, 74, 79, 102, 74, 74,
      97, 48, 103, 54, 80, 74, 87, 104, 51, 101, 84, 106, 82, 114, 57, 112, 81, 48, 70, 87, 111, 50,
      118, 88, 70, 108, 107, 83, 89, 83, 83, 83, 95, 121, 100, 118, 80, 105, 87, 112, 85, 95, 109,
      114, 100, 103, 100, 80, 105, 72, 48, 105, 53, 81, 48, 82, 85, 109, 114, 86, 73, 82, 104, 102,
      95, 53, 69, 66, 103, 103, 117, 67, 87, 65, 53, 115, 112, 81, 106, 76, 99, 50, 107, 114, 79,
      53, 100, 102, 113, 108, 75, 122, 117, 45, 119, 116, 103, 98, 53, 103, 74, 75, 89, 87, 45, 107,
      57, 54, 112, 95, 66, 90, 103, 118, 48, 109, 89, 84, 65, 97, 109, 82, 67, 121, 56, 98, 74, 72,
      109, 100, 105, 76, 90, 74, 80, 73, 105, 72, 74, 113, 74, 103, 67, 87, 56, 122, 97, 79, 53, 87,
      90, 86, 68, 82, 108, 66, 66, 120, 68, 75, 98, 90, 102, 120, 122, 119, 68, 97, 100, 88, 83, 89,
      115, 112, 49, 66, 101, 120, 69, 73, 118, 77};

  @BeforeClass
  public static void setUp() {
    stubFor(post(urlEqualTo("/postPacket"))
        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_OCTET_STREAM_VALUE))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
            .withStatus(OK.value())
            .withBody(gson.toJson(Response.ok(ImmutableMap.of(
                "code", "200"
            ))))));
  }

  @Before
  public void init() {
    when(dispatchException.getMessage())
        .thenReturn(RequestFactory.UNSUPPORTED_CMD_PREfIX + "any error");
    when(requestFactory.toRequest(any())).thenThrow(dispatchException);
    when(dispatcherProperties.getMockUrl())
        .thenReturn("http://localhost:" + WIRE_MOCK_RULE.port() + "/postPacket");
    //when(dispatcherProperties.getMockUrl())
    //    .thenReturn("http://temail-mock-api-manager.service.innertools.com/api/mock/match");
    //when(dispatcherProperties.getMockUrl())
    //    .thenReturn("http://172.31.240.202:8081/api/mock/match");
    //when(dispatcherProperties.getMockUrl())
    //    .thenReturn("http://localhost:8081/dispatch");
    when(dispAuthService.verify(any())).thenReturn(success);
    when(dispRawPacketDecoder.decode(any())).thenReturn(cdtpPacket);
  }

  @Test
  public void forwardRequest() throws Exception {
    ResponseEntity<String> responseEntity = dispDispatcherService.dispatch(data);
    Assertions.assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
  }

}