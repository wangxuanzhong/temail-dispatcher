package com.syswin.temail.cdtp.dispatcher.receive;

import com.google.gson.Gson;
import com.syswin.temail.cdtp.dispatcher.DispatcherApplication;
import com.syswin.temail.cdtp.dispatcher.receive.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.receive.entity.CDTPPackage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author 姚华成
 * @date 2018-8-9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DispatcherApplication.class)
public class DispatcherControllerTest {

    private MockMvc mvc;
    @Resource
    private WebApplicationContext wac;
    private Gson gson = new Gson();;

    @Before
    public void init() {
        if (mvc == null) {
            mvc = MockMvcBuilders.webAppContextSetup(wac).build();
        }
    }

    @Test
    public void testDispatchGetSimple() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testGet");
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchGetWithHeader() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testGet");
        Map<String, List<String>> headers = cdtpPackage.getData().getParams().getHeader();
        headers.put("headerName1", Collections.singletonList("headerValue1"));
        headers.put("headerName2", Arrays.asList("headerValue2", "headerValue3"));
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchGetWithQuery() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testGet");
        Map<String, List<String>> query = cdtpPackage.getData().getParams().getQuery();
        query.put("queryName1", Collections.singletonList("queryValue1"));
        query.put("queryName2", Arrays.asList("queryValue2", "queryValue3"));
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchGetWithAll() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testGet");
        Map<String, List<String>> headers = cdtpPackage.getData().getParams().getHeader();
        headers.put("headerName1", Collections.singletonList("headerValue1"));
        headers.put("headerName2", Arrays.asList("headerValue2", "headerValue3"));
        Map<String, List<String>> query = cdtpPackage.getData().getParams().getQuery();
        query.put("queryName1", Collections.singletonList("queryValue1"));
        query.put("queryName2", Arrays.asList("queryValue2", "queryValue3"));
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchPostWithHeader() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testPost");
        Map<String, List<String>> headers = cdtpPackage.getData().getParams().getHeader();
        headers.put("headerName1", Collections.singletonList("headerValue1"));
        headers.put("headerName2", Arrays.asList("headerValue2", "headerValue3"));
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchPostWithQuery() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testPost");
        Map<String, List<String>> query = cdtpPackage.getData().getParams().getQuery();
        query.put("queryName1", Collections.singletonList("queryValue1"));
        query.put("queryName2", Arrays.asList("queryValue2", "queryValue3"));
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchPostWithBody() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testPost");
        Map<String, Object> body = cdtpPackage.getData().getParams().getBody();
        body.put("f1", "value");
        body.put("f2",2);
        execReq(cdtpPackage);
    }

    @Test
    public void testDispatchPostWithAll() throws Exception {
        CDTPPackage cdtpPackage = initCDTPPackage();
        cdtpPackage.getData().setCommand("testPost");
        Map<String, List<String>> headers = cdtpPackage.getData().getParams().getHeader();
        headers.put("headerName1", Collections.singletonList("headerValue1"));
        headers.put("headerName2", Arrays.asList("headerValue2", "headerValue3"));
        Map<String, List<String>> query = cdtpPackage.getData().getParams().getQuery();
        query.put("queryName1", Collections.singletonList("queryValue1"));
        query.put("queryName2", Arrays.asList("queryValue2", "queryValue3"));
        Map<String, Object> body = cdtpPackage.getData().getParams().getBody();
        body.put("f1", "value");
        body.put("f2",2);
        execReq(cdtpPackage);
    }

    private void execReq(CDTPPackage cdtpPackage) throws Exception {
        String content = gson.toJson(cdtpPackage);
        System.out.println(content);
        RequestBuilder builder = MockMvcRequestBuilders.post("/dispatch")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(content);
        ResultActions actions = mvc.perform(builder);
        actions.andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals("SUCCESS", actions.andReturn().getResponse().getContentAsString());

    }

    private CDTPPackage initCDTPPackage() {
        CDTPPackage cdtpPackage = new CDTPPackage();
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
        params.setBody(new HashMap<>());
        cdtpBody.setParams(params);
        cdtpPackage.setData(cdtpBody);
        return cdtpPackage;
    }

}
