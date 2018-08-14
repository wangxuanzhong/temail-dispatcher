package com.syswin.temail.cdtp.dispatcher.request.application;

import com.google.gson.Gson;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;

public class CDTPWrapper {

  public CDTPPackage adapt(CDTPPackage payload, String body) {
    CDTPPackage result = new CDTPPackage();

    result.setCommand(payload.getCommand());
    result.setVersion(payload.getVersion());
    result.setAlgorithm(payload.getAlgorithm());
    result.setSign(payload.getSign());
    result.setDem(payload.getDem());
    result.setTimestamp(System.currentTimeMillis());
    result.setPkgId(payload.getPkgId());
    result.setFrom(payload.getFrom());
    result.setTo(payload.getTo());
    result.setSenderPK(payload.getSenderPK());
    result.setReceiverPK(payload.getReceiverPK());
    result.setData(new Gson().fromJson(body, CDTPBody.class));

    return result;
  }
}
