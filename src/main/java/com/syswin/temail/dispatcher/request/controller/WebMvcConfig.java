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
