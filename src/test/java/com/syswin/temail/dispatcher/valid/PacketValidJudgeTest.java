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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.DispRawPacketDecoder;
import com.syswin.temail.dispatcher.codec.PacketEncoder;
import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.dispatcher.request.entity.CDTPParams;
import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.dispatcher.valid.params.ValidParams;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class PacketValidJudgeTest {

  private CDTPPacket cdtpPacket;

  private final DispatcherProperties dispatcherProperties = new DispatcherProperties();

  private final PacketValidJudge packetValidJudge = new PacketValidJudge(dispatcherProperties);

  @Before
  public void init() {
    cdtpPacket = PacketMaker.privateMsgPacket("jack@t.email",
        "sean@t.email", "Sent ackMessage", "deviceId");

    dispatcherProperties.setValidStrategy(ImmutableMap.of(
        PacketValidType.commonSignValid.getMapKeycode(), ImmutableList.of("*-*"),
        PacketValidType.crossSingleSignValid.getMapKeycode(), ImmutableList.of("0001-*"),
        PacketValidType.crossTopicSignValid.getMapKeycode(), ImmutableList.of("000E-*"),
        PacketValidType.crossGroupsignValid.getMapKeycode(), ImmutableList.of("0002-*"),
        PacketValidType.skipSignValid.getMapKeycode(), ImmutableList.of("*-*","00EE-0001")));
  }


  @Test
  public void singleType(){
    cdtpPacket.setCommandSpace((short) 1);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isEqualTo(PacketValidType.crossSingleSignValid.getAuthUri());
    Assertions.assertThat(validParams.get().getParams()).isNotEmpty();

  }

  @Test
  public void groupType(){
    cdtpPacket.setCommandSpace((short) 2);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams.isPresent()).isTrue();

  }

  @Test
  public void topicType(){
    cdtpPacket.setCommandSpace((short) 0x000E);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isEqualTo(PacketValidType.crossTopicSignValid.getAuthUri());
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(5);
  }

  @Test
  public void commonType(){
    cdtpPacket.setCommandSpace((short) 0x0016);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams).isPresent();
    Assertions.assertThat(validParams.get().getAuthUri()).isEqualTo(PacketValidType.commonSignValid.getAuthUri());
    Assertions.assertThat(validParams.get().getParams().size()).isEqualTo(4);
  }


  @Test
  public void skipType(){
    cdtpPacket.setCommandSpace((short) 0x00EE);
    cdtpPacket.setCommand((short) 1);
    cdtpPacket.setData(new PacketEncoder().encode(cdtpPacket));
    Optional<ValidParams> validParams = packetValidJudge.buildParams(cdtpPacket, t -> "i am singature");
    Assertions.assertThat(validParams.isPresent()).isFalse();
  }


  @Test
  public  void test(){

    byte[] aa =  new byte[]{0, 0, 2, -37, 0, 15, 1, 9, 0, 2, 2, 87, 10,
        64, 48, 52, 65, 70, 48, 52, 52, 70, 45, 49, 67, 67, 70, 45, 52, 56, 68, 70, 45, 56, 69, 54, 50, 45, 57, 55, 53, 67, 49, 70, 51, 52, 68, 70, 57, 70, 109, 115, 103, 115, 101, 97, 108, 46, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 58, 56, 48, 57, 57, 26, -70, 1, 77, 73, 71, 73, 65, 107, 73, 66, 55, 101, 48, 76, 100, 97, 115, 75, 118, 49, 116, 67, 78, 80, 118, 102, 50, 54, 122, 79, 105, 50, 100, 87, 86, 86, 97, 97, 83, 104, 80, 76, 97, 85, 120, 87, 113, 80, 105, 65, 77, 78, 50, 73, 48, 99, 87, 111, 78, 82, 73, 103, 95, 55, 105, 74, 53, 54, 98, 120, 89, 80, 75, 103, 110, 67, 45, 109, 110, 103, 95, 54, 48, 97, 72, 74, 122, 112, 79, 120, 84, 113, 48, 65, 111, 120, 115, 67, 81, 103, 71, 85, 98, 77, 99, 56, 78, 67, 54, 109, 121, 115, 76, 49, 102, 106, 118, 87, 71, 118, 111, 68, 66, 49, 68, 113, 88, 67, 105, 102, 114, 108, 98, 83, 87, 101, 77, 110, 67, 100, 118, 55, 69, 82, 112, 70, 122, 45, 90, 66, 119, 95, 73, 89, 116, 68, 65, 79, 97, 119, 85, 104, 71, 78, 78, 45, 118, 105, 45, 53, 87, 84, 99, 113, 116, 49, 66, 50, 73, 89, 105, 72, 55, 79, 88, 122, 51, 119, 41, -55, 71, 56, 93, 0, 0, 0, 0, 50, 36, 56, 56, 67, 68, 55, 66, 66, 54, 45, 68, 51, 54, 68, 45, 52, 70, 48, 51, 45, 56, 66, 66, 51, 45, 54, 53, 55, 55, 48, 70, 57, 70, 66, 69, 53, 66, 58, 21, 102, 115, 48, 48, 55, 64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 66, -45, 1, 77, 73, 71, 98, 77, 66, 65, 71, 66, 121, 113, 71, 83, 77, 52, 57, 65, 103, 69, 71, 66, 83, 117, 66, 66, 65, 65, 106, 65, 52, 71, 71, 65, 65, 81, 66, 110, 81, 120, 95, 51, 51, 109, 66, 121, 116, 112, 122, 88, 122, 98, 51, 66, 88, 55, 69, 81, 72, 108, 69, 73, 99, 90, 88, 84, 48, 120, 54, 65, 52, 114, 114, 120, 110, 50, 112, 45, 81, 83, 105, 87, 87, 112, 74, 82, 115, 113, 54, 107, 70, 55, 68, 75, 50, 122, 51, 98, 95, 108, 98, 49, 49, 53, 77, 97, 106, 50, 49, 71, 80, 95, 85, 79, 67, 108, 108, 88, 110, 109, 57, 103, 106, 107, 65, 76, 67, 49, 77, 73, 89, 118, 45, 95, 104, 77, 121, 110, 51, 106, 101, 69, 99, 95, 116, 72, 108, 71, 50, 75, 51, 111, 97, 90, 74, 111, 115, 113, 68, 113, 68, 75, 69, 103, 45, 97, 52, 52, 88, 69, 100, 107, 100, 118, 90, 97, 106, 53, 90, 100, 119, 49, 70, 73, 52, 90, 45, 101, 120, 86, 102, 69, 105, 49, 109, 70, 98, 108, 112, 65, 55, 88, 80, 97, 120, 107, 80, 45, 113, 101, 72, 107, 74, 28, 99, 46, 50, 49, 50, 53, 51, 54, 48, 56, 50, 50, 64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 114, 28, 109, 115, 103, 115, 101, 97, 108, 46, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 58, 56, 48, 57, 57, 123, 10, 9, 34, 113, 117, 101, 114, 121, 34, 32, 58, 32, 10, 9, 123, 10, 9, 9, 34, 102, 114, 111, 109, 34, 32, 58, 32, 34, 102, 115, 48, 48, 55, 64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 34, 44, 10, 9, 9, 34, 103, 114, 111, 117, 112, 84, 101, 109, 97, 105, 108, 34, 32, 58, 32, 34, 99, 46, 50, 49, 50, 53, 51, 54, 48, 56, 50, 50, 64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 34, 44, 10, 9, 9, 34, 115, 116, 97, 116, 117, 115, 34, 32, 58, 32, 45, 50, 10, 9, 125, 10, 125, 10};

    DispRawPacketDecoder dispRawPacketDecoder = new DispRawPacketDecoder(null);
    CDTPPacket decode = dispRawPacketDecoder.decode(aa);
    System.out.println("xxx");

  }

  @Test
  public void test2(){
    String s = "{\n"
        + "\t\"query\" : \n"
        + "\t{\n"
        + "\t\t\"from\" : \"fs007@systoontest.com\",\n"
        + "\t\t\"groupTemail\" : \"c.2125360822@systoontest.com\",\n"
        + "\t\t\"status\" : -2\n"
        + "\t},\n"
        + "\t\"body\":\n"
        + "\t{\n"
        + "\n"
        + "\t}\n"
        + "}";
    Gson gson = new Gson();
    CDTPParams cdtpParams = gson.fromJson(s, CDTPParams.class);
    System.out.println("xxx");
  }


}