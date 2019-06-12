package com.syswin.temail.dispatcher.request.application;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class UriComponentsBuilderTest {

  private UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
      .fromHttpUrl("http://localhost:8081/books/{bookId}/order/{userId}");

  @Test
  public void queryParamBuild(){
    Map<String,Object> queryParam = new HashMap<>();
    queryParam.put("name","zhangsan");
    queryParam.put("school","保定军校");
    queryParam.put("age","23");
    for (Map.Entry en: queryParam.entrySet()) {
      this.uriComponentsBuilder.queryParam(en.getKey().toString(), en.getValue().toString());
    }
    log.info(this.uriComponentsBuilder.toUriString());

    Map<String,Object> urivariable = new HashMap<>();
    urivariable.put("bookId","1223");
    urivariable.put("userId","user123");
    this.uriComponentsBuilder.uriVariables(urivariable);
    log.info(this.uriComponentsBuilder.toUriString());
  }

}