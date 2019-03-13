package com.syswin.temail.dispatcher.request;

import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Base64;
import java.util.function.BiPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("pact")
@Configuration
class PacketDecoderConfig {

  @Primary
  @Bean
  DispRawPacketDecoder decoder(BiPredicate<Short, Short> predicate) {
    return new DispRawPacketDecoder(predicate) {
      @Override
      public CDTPPacket decode(byte[] encodedBytes) {
        return super.decode(Base64.getUrlDecoder().decode(encodedBytes));
      }};
  }
}
