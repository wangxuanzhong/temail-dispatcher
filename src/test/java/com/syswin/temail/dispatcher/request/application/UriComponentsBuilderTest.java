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