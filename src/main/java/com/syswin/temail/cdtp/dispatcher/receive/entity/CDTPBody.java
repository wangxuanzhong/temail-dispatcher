package com.syswin.temail.cdtp.dispatcher.receive.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 姚华成
 * @date 2018/8/8
 */
@Data
public class CDTPBody {
    private String command;
    private CDTPParams params;

    @Data
    public static class CDTPParams {
        private Map<String, List<String>> header;
        private Map<String, List<String>> query;
        private Map<String, Object> body;
    }
}
