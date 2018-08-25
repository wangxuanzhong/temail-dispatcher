package com.syswin.temail.dispatcher.request;

import com.syswin.temail.dispatcher.DispatcherApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 姚华成
 * @date 2018-8-9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DispatcherApplication.class)
public class DispatchController2Test {
//
//  private MockMvc mvc;
//  @Resource
//  private WebApplicationContext wac;
//  private Gson gson = new Gson();
//
//  @Before
//  public void init() {
//    if (mvc == null) {
//      mvc = MockMvcBuilders.webAppContextSetup(wac).build();
//    }
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchGetSimple() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(101);
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchGetWithHeader() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(101);
//    Map<String, String> headers = new HashMap<>(2);
//    headers.put("headerName1", "headerValue1");
//    headers.put("headerName2", "headerValue2");
//    CDTPParams params = new CDTPParams();
//    params.setHeader(headers);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchGetWithQuery() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(101);
//    Map<String, String> query = new HashMap<>(2);
//    query.put("queryName1", "queryValue1");
//    query.put("queryName2", "queryValue2");
//    CDTPParams params = new CDTPParams();
//    params.setQuery(query);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchGetWithAll() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(101);
//    Map<String, String> headers = new HashMap<>(2);
//    headers.put("headerName1", "headerValue1");
//    headers.put("headerName2", "headerValue2");
//    Map<String, String> query = new HashMap<>(2);
//    query.put("queryName1", "queryValue1");
//    query.put("queryName2", "queryValue2");
//    CDTPParams params = new CDTPParams();
//    params.setHeader(headers);
//    params.setQuery(query);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchPostWithHeader() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(102);
//    Map<String, String> headers = new HashMap<>();
//    headers.put("headerName1", "headerValue1");
//    headers.put("headerName2", "headerValue2");
//    CDTPParams params = new CDTPParams();
//    params.setHeader(headers);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchPostWithQuery() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(102);
//    Map<String, String> query = new HashMap<>();
//    query.put("queryName1", "queryValue1");
//    query.put("queryName2", "queryValue2");
//    CDTPParams params = new CDTPParams();
//    params.setQuery(query);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchPostWithBody() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(102);
//    Map<String, Object> body = new HashMap<>(2);
//    body.put("f1", "value");
//    body.put("f2", 2);
//    CDTPParams params = new CDTPParams();
//    params.setBody(body);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Ignore
//  @Test
//  public void testDispatchPostWithAll() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(102);
//    Map<String, String> headers = new HashMap<>(2);
//    headers.put("headerName1", "headerValue1");
//    headers.put("headerName2", "headerValue2");
//    Map<String, String> query = new HashMap<>(2);
//    query.put("queryName1", "queryValue1");
//    query.put("queryName2", "queryValue2");
//    Map<String, Object> body = new HashMap<>(2);
//    body.put("f1", "value");
//    body.put("f2", 2);
//    CDTPParams params = new CDTPParams();
//    params.setHeader(headers);
//    params.setQuery(query);
//    params.setBody(body);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Test
//  public void testUsermailGet() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(1001);
//    CDTPParams params = new CDTPParams();
//    Map<String, String> query = new HashMap<>();
//    query.put("from", "yaohuacheng@syswin.com");
//    query.put("to", "wangxuanzhong@syswin.com");
//    query.put("pageSize", "10");
//    query.put("fromSeqId", "0");
//    params.setQuery(query);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  @Test
//  public void testUsermailPost() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(1002);
//    CDTPParams params = new CDTPParams();
//    Map<String, Object> body = new HashMap<>(10);
//    body.put("from", "yaohuacheng@syswin.com");
//    body.put("fromMsg", "string");
//    body.put("msgid", "string");
//    body.put("seqNo", 0);
//    body.put("to", "wangxuanzhong@syswin.com");
//    body.put("toMsg", "string");
//    body.put("type", 0);
//    params.setBody(body);
//    cdtpPackage.setData(gson.toJson(params));
//
//    execReq(cdtpPackage);
//  }
//
//  @Test
//  public void testRevertPost() throws Exception {
//    CDTPPackage cdtpPackage = initCDTPPackage();
//    cdtpPackage.setCommand(1003);
//    CDTPParams params = new CDTPParams();
//    Map<String, Object> body = new HashMap<>(10);
//    body.put("from", "yaohuacheng@syswin.com");
//    body.put("fromMsg", "string");
//    body.put("msgid", "syswin-1534131915194-4");
//    body.put("seqNo", 3);
//    body.put("to", "wangxuanzhong@syswin.com");
//    body.put("toMsg", "string");
//    body.put("type", 0);
//    params.setBody(body);
//    cdtpPackage.setData(gson.toJson(params));
//    execReq(cdtpPackage);
//  }
//
//  private void execReq(CDTPPackage cdtpPackage) throws Exception {
//    String content = gson.toJson(cdtpPackage);
//    System.out.println(content);
//    RequestBuilder builder = MockMvcRequestBuilders.post("/dispatch")
//        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//        .content(content);
//    ResultActions actions = mvc.perform(builder);
//    actions.andExpect(MockMvcResultMatchers.status().isOk());
//    Assert.assertEquals("SUCCESS", actions.andReturn().getResponse().getContentAsString());
//
//  }
//
//  private CDTPPackage initCDTPPackage() {
//    CDTPPackage cdtpPackage = new CDTPPackage();
//    cdtpPackage.setCommand(1);
//    cdtpPackage.setVersion(1);
//    cdtpPackage.setAlgorithm(1);
//    cdtpPackage.setSign("sign");
//    cdtpPackage.setDem(1);
//    cdtpPackage.setTimestamp(System.currentTimeMillis());
//    cdtpPackage.setPkgId("pkgId");
//    cdtpPackage.setFrom("yaohuacheng@syswin.com");
//    cdtpPackage.setTo("yaohuacheng@syswin.com");
//    cdtpPackage.setSenderPK("SenderPK(");
//    cdtpPackage.setReceiverPK("ReceiverPK(");
//
//    return cdtpPackage;
//  }

}
