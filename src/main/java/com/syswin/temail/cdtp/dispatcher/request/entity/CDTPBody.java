package com.syswin.temail.cdtp.dispatcher.request.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CDTPBody {
    private String command;
    private CDTPParams params;

    @Data
    public static class CDTPParams {
        private Map<String, List<String>> header;
        private Map<String, List<String>> query;
        private String body;
    }
}
