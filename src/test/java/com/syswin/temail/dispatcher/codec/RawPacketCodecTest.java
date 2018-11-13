package com.syswin.temail.dispatcher.codec;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.function.BiPredicate;
import org.junit.Test;
import org.mockito.Mockito;

public class RawPacketCodecTest {

  @SuppressWarnings("unchecked")
  private final BiPredicate<Short, Short> commandAwarePredicate = Mockito.mock(BiPredicate.class);

  private final String sender = "sender@t.email";
  private final String deviceId = uniquify("deviceId");

  private final PacketEncoder encoder = new PacketEncoder();
  private final RawPacketDecoder decoder = new RawPacketDecoder(commandAwarePredicate);
  private final CDTPPacket packet = PacketMaker.privateMsgPacket(sender, "recipient", "hello world", deviceId);

  @Test
  public void includeFullPacketBytesIfPredicated() {
    when(commandAwarePredicate.test(packet.getCommandSpace(), packet.getCommand())).thenReturn(true);

    byte[] encoded = encoder.encode(packet);
    CDTPPacket decodedPacket = decoder.decode(encoded);

    assertThat(decodedPacket.getCommandSpace()).isEqualTo(packet.getCommandSpace());
    assertThat(decodedPacket.getCommand()).isEqualTo(packet.getCommand());
    assertThat(decodedPacket.getHeader().getDeviceId()).isEqualTo(packet.getHeader().getDeviceId());
    assertThat(decodedPacket.getHeader().getSender()).isEqualTo(packet.getHeader().getSender());

    decodedPacket = decoder.decode(decodedPacket.getData());

    assertThat(decodedPacket).isEqualToIgnoringGivenFields(packet, "data");
  }

  @Test
  public void doNotIncludeFullPacketIfNotPredicated() {
    when(commandAwarePredicate.test(packet.getCommandSpace(), packet.getCommand())).thenReturn(false);

    byte[] encoded = encoder.encode(packet);
    CDTPPacket decodedPacket = decoder.decode(encoded);

    assertThat(decodedPacket).isEqualTo(packet);
  }
}
