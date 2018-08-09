package com.syswin.temail.cdtp.dispatcher.receive.controller;

import com.google.gson.GsonBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author 姚华成
 * @date 2018-8-9
 */
@Component
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 将MappingJackson2HttpMessageConverter和默认的GsonHttpMessageConverter都删除
        // 创建自定义的GsonHttpMessageConverter
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter
                || converter instanceof GsonHttpMessageConverter);
        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        GsonBuilder builder = new GsonBuilder();
        gsonConverter.setGson(builder.create());
        converters.add(gsonConverter);
    }
}
