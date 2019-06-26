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
  private final DispRawPacketDecoder decoder = new DispRawPacketDecoder(commandAwarePredicate);
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
