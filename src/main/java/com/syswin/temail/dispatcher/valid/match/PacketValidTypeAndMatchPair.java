package com.syswin.temail.dispatcher.valid.match;

import lombok.Getter;

@Getter
public class PacketValidTypeAndMatchPair {

  private PacketValidType packetValidType;

  private PacketValidMatchType packetValidMatchType;

  public PacketValidTypeAndMatchPair(PacketValidType packetValidType,
      PacketValidMatchType packetValidMatchType) {
    this.packetValidType = packetValidType;
    this.packetValidMatchType = packetValidMatchType;
  }

}
