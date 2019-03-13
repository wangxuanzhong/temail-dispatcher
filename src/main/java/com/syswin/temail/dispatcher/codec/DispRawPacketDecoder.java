package com.syswin.temail.dispatcher.codec;

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.BiPredicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispRawPacketDecoder {

  private final BiPredicate<Short, Short> commandAwarePredicate;

  public DispRawPacketDecoder(BiPredicate<Short, Short> commandAwarePredicate) {
    this.commandAwarePredicate = commandAwarePredicate;
  }

  public CDTPPacket decode(byte[] encodedBytes) {
    try {
      ByteBuffer byteBuffer = ByteBuffer.wrap(encodedBytes);
      CDTPPacket packet = new CDTPPacket();

      byteBuffer.mark();
      int packetLength = readPacketLength(byteBuffer);

      readCommandSpace(byteBuffer, packet);
      readCommand(byteBuffer, packet);
      readVersion(byteBuffer, packet);
      short headerLength = readHeader(byteBuffer, packet);

      readData(byteBuffer, packet, packetLength, headerLength);

      log.info("Received packet：CommandSpace={}, Command={}, CDTPHeader={}, Data={}, DataLength={} .",
          packet.getCommandSpace(),
          packet.getCommand(),
          packet.getHeader(),
          new String(packet.getData()),
          packet.getData().length);

      return packet;

    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to decode packet", e);
    }
  }

  private int readPacketLength(ByteBuffer byteBuffer) {
    return byteBuffer.getInt();
  }

  private void readCommandSpace(ByteBuffer byteBuffer, CDTPPacket packet) {
    short commandSpace = byteBuffer.getShort();
    packet.setCommandSpace(commandSpace);
  }

  private void readCommand(ByteBuffer byteBuffer, CDTPPacket packet) {
    short command = byteBuffer.getShort();
    packet.setCommand(command);
  }

  private void readVersion(ByteBuffer byteBuffer, CDTPPacket packet) {
    short version = byteBuffer.getShort();
    packet.setVersion(version);
  }

  private short readHeader(ByteBuffer byteBuffer, CDTPPacket packet) throws IOException {
    short headerLength = byteBuffer.getShort();
    CDTPProtoBuf.CDTPHeader cdtpHeader;

    if (headerLength > 0) {
      byte[] headerBytes = new byte[headerLength];
      byteBuffer.get(headerBytes);
      cdtpHeader = CDTPProtoBuf.CDTPHeader.parseFrom(headerBytes);
      packet.setHeader(new CDTPHeader(cdtpHeader));
    }
    return headerLength;
  }

  private void readData(ByteBuffer byteBuffer, CDTPPacket packet, int packetLength, short headerLength) {
    byte[] data;
    if (!commandAwarePredicate.test(packet.getCommandSpace(), packet.getCommand())) {
      data = new byte[packetLength - headerLength - 8];
    } else {
      byteBuffer.reset();
      data = new byte[packetLength + LENGTH_FIELD_LENGTH];
    }
    byteBuffer.get(data);
    packet.setData(data);
  }
}
