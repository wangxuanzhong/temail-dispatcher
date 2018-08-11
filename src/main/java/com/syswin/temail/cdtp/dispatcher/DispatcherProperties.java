package com.syswin.temail.cdtp.dispatcher;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

@Data
@ConfigurationProperties(prefix = "temail.cdtp.dispatcher")
public class DispatcherProperties {
    private Map<String, Request> cmdRequestMap = new HashMap<>();
    private String authVerifyUrl;

    @Data
    public static class Request {
        private String url;
        private HttpMethod method;
    }
}
