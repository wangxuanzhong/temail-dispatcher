package com.syswin.temail.cdtp.dispatcher;

import com.syswin.temail.cdtp.dispatcher.notify.RocketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author 姚华成
 * @date 2018/8/8
 */
@SpringBootApplication
@EnableConfigurationProperties({DispatcherProperties.class, RocketProperties.class})
public class DispatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(DispatcherApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        builder.setConnectTimeout(3000);
        builder.setReadTimeout(3000);
        return builder.build();
    }
}
