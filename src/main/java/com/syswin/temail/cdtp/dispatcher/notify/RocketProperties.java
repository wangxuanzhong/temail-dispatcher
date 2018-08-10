package com.syswin.temail.cdtp.dispatcher.notify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Data
@ConfigurationProperties(prefix = "temail.cdtp.dispatcher.rocketmq")
public class RocketProperties {
    private String namesrvAddr;
    private String producerGroup = "cdtp-dispatcher-producer";
    private String consumerGroup = "cdtp-dispatcher-consumer";
    private String topic = "cdtp-notify";
}
