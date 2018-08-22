package com.syswin.temail.dispatcher.request;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.request.entity.CDTPPackage;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 姚华成
 * @date 2018-8-15
 */
public class TestMain {

  private static CDTPPackage initCDTPPackage() {
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

    return cdtpPackage;
  }

  public static void main(String[] args) {
    Gson gson = new Gson();
    CDTPPackage cdtpPackage = initCDTPPackage();
    cdtpPackage.setCommand(102);
    Map<String, String> headers = new HashMap<>(2);
    headers.put("headerName1", "headerValue1");
    headers.put("headerName2", "headerValue2");
    Map<String, String> query = new HashMap<>(2);
    query.put("queryName1", "queryValue1");
    query.put("queryName2", "queryValue2");
    Map<String, Object> body = new HashMap<>(2);
    body.put("f1", "value");
    body.put("f2", 2);
    CDTPParams params = new CDTPParams();
    params.setHeader(headers);
    params.setQuery(query);
    params.setBody(body);
    cdtpPackage.setData(gson.toJson(params));
    System.out.println(gson.toJson(cdtpPackage));
  }
}
