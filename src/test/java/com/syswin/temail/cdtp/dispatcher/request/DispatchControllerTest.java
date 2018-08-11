package com.syswin.temail.cdtp.dispatcher.request;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.syswin.temail.cdtp.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

@RunWith(SpringRunner.class)
@WebMvcTest
public class DispatchControllerTest {

  @MockBean
  private PackageDispatcher packageDispatcher;

  @Autowired
  private MockMvc mvc;
  private Gson gson = new Gson();

  @Test
  public void wrapResponse() throws Exception {
    when(packageDispatcher.dispatch(any())).thenReturn(ResponseEntity.ok("hello"));

    CDTPPackage<CDTPBody> cdtpPackage = initCDTPPackage();
    String content = gson.toJson(cdtpPackage);

    mvc.perform(post("/dispatch")
        .contentType(APPLICATION_JSON_UTF8_VALUE)
        .content(content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", is("hello")))
        .andExpect(jsonPath("$.command", is(1)))
        .andExpect(jsonPath("$.version", is(1)))
        .andExpect(jsonPath("$.algorithm", is(1)))
        .andExpect(jsonPath("$.sign", is("sign")))
        .andExpect(jsonPath("$.dem", is(1)))
        .andExpect(jsonPath("$.pkgId", is("pkgId")))
        .andExpect(jsonPath("$.from", is("yaohuacheng@syswin.com")))
        .andExpect(jsonPath("$.to", is("yaohuacheng@syswin.com")))
        .andExpect(jsonPath("$.senderPK", is("SenderPK(")))
        .andExpect(jsonPath("$.receiverPK", is("ReceiverPK(")));
  }

  private CDTPPackage<CDTPBody> initCDTPPackage() {
    CDTPPackage<CDTPBody> cdtpPackage = new CDTPPackage<>();
    cdtpPackage.setCommand(1);
    cdtpPackage.setVersion(1);
    cdtpPackage.setAlgorithm(1);
    cdtpPackage.setSign("sign");
    cdtpPackage.setDem(1);
    cdtpPackage.setTimestamp(System.currentTimeMillis());
    cdtpPackage.setPkgId("pkgId");
    cdtpPackage.setFrom("yaohuacheng@syswin.com");
    cdtpPackage.setTo("yaohuacheng@syswin.com");
    cdtpPackage.setSenderPK("SenderPK(");
    cdtpPackage.setReceiverPK("ReceiverPK(");

    CDTPBody cdtpBody = new CDTPBody();

    CDTPBody.CDTPParams params = new CDTPBody.CDTPParams();

    params.setHeader(new LinkedMultiValueMap<>());
    params.setQuery(new LinkedMultiValueMap<>());
    cdtpBody.setParams(params);
    cdtpPackage.setData(cdtpBody);
    return cdtpPackage;
  }
}
