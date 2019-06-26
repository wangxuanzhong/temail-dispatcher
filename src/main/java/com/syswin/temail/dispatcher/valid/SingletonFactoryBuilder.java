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

package com.syswin.temail.dispatcher.valid;

import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.dispatcher.valid.params.ValidParamsCommonFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsCrossGroupFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsCrossSingleFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsSkipFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsTopicFactory;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactoryBuilder {

  private final Map<PacketValidType, ValidParamsFactory> cache = new ConcurrentHashMap<>();

  public Optional<ValidParamsFactory> build(PacketValidType packetValidType){
    switch (packetValidType){
      case crossSingleSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.crossSingleSignValid, t -> new ValidParamsCrossSingleFactory()));
      }
      case crossGroupsignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.crossGroupsignValid, t -> new ValidParamsCrossGroupFactory()));
      }
      case crossTopicSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.crossTopicSignValid, t -> new ValidParamsTopicFactory()));
      }
      case skipSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.skipSignValid, t -> new ValidParamsSkipFactory()));
      }
      case commonSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.commonSignValid, t -> new ValidParamsCommonFactory()));
      }
      default:{
        return Optional.empty();
      }
    }
  }
}
