package com.syswin.temail.cdtp.dispatcher.receive.controller;

import com.syswin.temail.cdtp.dispatcher.DispatcherProperties;
import com.syswin.temail.cdtp.dispatcher.receive.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.receive.entity.CDTPPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author 姚华成
 * @date 2018/8/8
 */
@Slf4j
@RestController
public class DispatcherController {
    public static final String CONTENT_TYPE = "Content-Type";
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private DispatcherProperties properties;

    @PostMapping(value = "/dispatch")
    public String dispatch(@RequestBody CDTPPackage cdtpPackage) {
        CDTPBody cdtpBody = cdtpPackage.getData();
        CDTPBody.CDTPParams params = cdtpBody.getParams();
        DispatcherProperties.Request request =
                properties.getCmdRequestMap().get(cdtpBody.getCommand());
        if (request == null) {
            log.error("不支持的命令类型：{}\n请求参数：{}", cdtpBody.getCommand(), cdtpPackage);
            throw new RuntimeException("不支持的命令类型：" + cdtpBody.getCommand());
        }
        Map<String, List<String>> paramsHeader = params.getHeader();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        if (paramsHeader != null && !paramsHeader.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : paramsHeader.entrySet()) {
                headers.addAll(entry.getKey(), entry.getValue());
            }
        }
        addCDTPHeaders(headers, cdtpPackage);
        HttpEntity<Map<String, Object>> entity;
        switch (request.getMethod()) {
            case GET:
            case DELETE:
                entity = new HttpEntity<>(headers);
                break;
            case POST:
            case PUT:
                Map<String, Object> body = params.getBody();
                if (body != null) {
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
                    entity = new HttpEntity<>(body, headers);
                } else {
                    entity = new HttpEntity<>(headers);
                }
                break;
            default:
                log.error("请求参数：{}", cdtpPackage);
                throw new RuntimeException("不支持的命令类型：" + cdtpBody.getCommand());
        }
        String url = request.getUrl();
        Map<String, List<String>> query = params.getQuery();
        if (query != null && !query.isEmpty()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            for (Map.Entry<String, List<String>> entry : query.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue().toArray());
            }
            url = builder.toUriString();
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, request.getMethod(), entity, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            // TODO(姚华成) 异常信息需要完善
            log.error("请求分发出错！返回码是" + responseEntity.getStatusCodeValue());
        }
        return "SUCCESS";
    }

    private void addCDTPHeaders(MultiValueMap<String, String> headers, CDTPPackage cdtpPackage) {
        headers.add("command", String.valueOf(cdtpPackage.getCommand()));
        headers.add("version", String.valueOf(cdtpPackage.getVersion()));
        headers.add("algorithm", String.valueOf(cdtpPackage.getAlgorithm()));
        headers.add("sign", cdtpPackage.getSign());
        headers.add("dem", String.valueOf(cdtpPackage.getDem()));
        headers.add("timestamp", String.valueOf(cdtpPackage.getTimestamp()));
        headers.add("pkgId", cdtpPackage.getPkgId());
        headers.add("from", cdtpPackage.getFrom());
        headers.add("to", cdtpPackage.getTo());
        headers.add("senderPK", cdtpPackage.getReceiverPK());
        headers.add("receiverPK", cdtpPackage.getSenderPK());
    }

}
