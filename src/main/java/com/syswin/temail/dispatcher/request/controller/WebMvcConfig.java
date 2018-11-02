package com.syswin.temail.dispatcher.request.controller;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import java.util.List;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.spring.web.json.Json;

@Component
public class WebMvcConfig implements WebMvcConfigurer {

  private final boolean swaggerJsonPresent =
      ClassUtils.isPresent("springfox.documentation.spring.web.json.Json",
          getClass().getClassLoader());

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // swagger比较特殊，需要使用用专用的序列化类
    // 将MappingJackson2HttpMessageConverter和默认的GsonHttpMessageConverter都删除
    // 创建自定义的GsonHttpMessageConverter
    converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter
        || converter instanceof GsonHttpMessageConverter);
    GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
    GsonBuilder builder = new GsonBuilder();
    if (swaggerJsonPresent) {
      builder.registerTypeAdapter(Json.class, (JsonSerializer<Json>) (json, type, context) -> {
        if (json == null) {
          return null;
        }
        return new JsonParser().parse(json.value());
      });
    }
    gsonConverter.setGson(builder.create());
    converters.add(gsonConverter);
  }
}
