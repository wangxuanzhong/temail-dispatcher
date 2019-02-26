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