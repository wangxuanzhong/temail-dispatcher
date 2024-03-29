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
import com.syswin.temail.dispatcher.valid.params.ValidParams;
import com.syswin.temail.dispatcher.valid.params.ValidParamsFactory;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class SingletonFactoryBuilderTest {

  private final SingletonFactoryBuilder singletonFactoryBuilder = new SingletonFactoryBuilder();

  private final Map<PacketValidType, ValidParamsFactory> localCache = new HashMap<>();

  private final ValidParamsFactory defaultFactory = new ValidParamsFactory() {
    @Override
    public Optional<ValidParams> buildParams(CDTPPacket cdtpPacket, Function<CDTPPacket, String> signExtract) {
      return Optional.empty();
    }
  };

  @Before
  public void init(){
    for(PacketValidType packetValidType : PacketValidType.values()){
      localCache.put(packetValidType,singletonFactoryBuilder.build(packetValidType).orElse(defaultFactory));
    }
  }

  @Test
  public void test() {
    for (int i = 0; i < 10; i++) {
      for(PacketValidType packetValidType : PacketValidType.values()){
        Assertions.assertThat(localCache.get(packetValidType) == singletonFactoryBuilder.build(packetValidType).get()).isTrue();
      }
    }

  }

}