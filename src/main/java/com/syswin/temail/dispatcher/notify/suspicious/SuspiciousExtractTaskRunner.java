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

package com.syswin.temail.dispatcher.notify.suspicious;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.notify.NotificationMessageFactory;
import com.syswin.temail.dispatcher.request.controller.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class SuspiciousExtractTaskRunner implements TaskExecutor<CDTPHeader>, CommandLineRunner {

  private final BlockingQueue<CDTPHeader> blockingQueue = new LinkedBlockingQueue<>();
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final ParameterizedTypeReference<Response<Map>> responseType = responseType();
  private final NotificationMessageFactory notificationMessageFactory;
  private final DispatcherProperties dispatcherProperties;
  private final String suspiciousKeyInMsg = "suspicious";
  private final Consumer<Boolean> resultConsumer;
  private final String suspiciousValInMsg = "1.0";
  private final RestTemplate restTemplate;
  private final String suspiciousRegisterUri = "/relation/suspicious";

  public SuspiciousExtractTaskRunner(RestTemplate restTemplate,
      NotificationMessageFactory notificationMessageFactory,
      DispatcherProperties dispatcherProperties,
      Consumer<Boolean> resultConsumer) {
    this.notificationMessageFactory = notificationMessageFactory;
    this.dispatcherProperties = dispatcherProperties;
    this.resultConsumer = resultConsumer;
    this.restTemplate = restTemplate;
  }

  @Override
  public void run(String... args) throws Exception {
    this.executorService.submit(this::extractAndRegister);
  }

  private void extractAndRegister() {
    log.info("Ready to start suspicious extract task.");
    while (!Thread.currentThread().isInterrupted()) {
      CDTPHeader cdtpHeader = null;
      try {
        cdtpHeader = this.take();
        this.resultConsumer.accept(this.handleTask(cdtpHeader));
      } catch (InterruptedException e) {
        log.error("InterruptedException was caught, exit task executing loop!", e);
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        log.warn("Exception was caught while handle task:{} ",
            cdtpHeader == null ? "" : cdtpHeader.toString(), e);
      }
    }
  }

  @Override
  public boolean handleTask(CDTPHeader cdtpHeader) {
    Optional<RelationBind> relationBind = this.extract(cdtpHeader);
    if (!relationBind.isPresent()) {
      log.warn("Fail to extract suspicious info from :{}", cdtpHeader.toString());
      return false;
    }

    RelationBind relation = relationBind.get();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
    Map<String, Object> map = new HashMap<>();
    map.put("sender", relation.getSender());
    map.put("receiver", relation.getReceiver());
    map.put("contactType", relation.getContactType());
    ResponseEntity<Response<Map>> result = restTemplate.exchange(
        dispatcherProperties.getRelationBaseUrl() + suspiciousRegisterUri,
        HttpMethod.POST, new HttpEntity<>(map, httpHeaders), responseType);

    if (!result.getStatusCode().is2xxSuccessful()) {
      log.warn("Fail to register: {} to relation server with msg: {}.",
          relation.toString(), String.valueOf(result.getBody().getMessage()));
      return false;
    } else {
      log.info("Succeed to register: {} to relation server.",
          relation.toString());
      return true;
    }
  }

  public Optional<RelationBind> extract(CDTPHeader cdtpHeader) {
    Map map = this.notificationMessageFactory.getExtraData(cdtpHeader);
    if (!(map.containsKey(suspiciousKeyInMsg) &&
        suspiciousValInMsg.equals(String.valueOf(map.get(suspiciousKeyInMsg))))) {
      return Optional.empty();
    } else {
      String to = (String) map.get("to");
      String from = (String) map.get("from");
      if (StringUtils.isEmpty(from) || StringUtils.isEmpty(to)) {
        return Optional.empty();
      }
      return Optional.of(new RelationBind(to, from, RelationType.suspicious.getCode()));
    }
  }

  @Override
  public boolean offer(CDTPHeader cdtpHeader) {
    return this.blockingQueue.offer(cdtpHeader);
  }

  @Override
  public CDTPHeader take() throws InterruptedException {
    return this.blockingQueue.take();
  }

  private ParameterizedTypeReference<Response<Map>> responseType() {
    return new ParameterizedTypeReference<Response<Map>>() {
    };
  }

  public boolean isEmpty() {
    return this.blockingQueue.isEmpty();
  }

}
