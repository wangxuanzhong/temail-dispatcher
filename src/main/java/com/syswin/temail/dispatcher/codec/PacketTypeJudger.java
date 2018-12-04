package com.syswin.temail.dispatcher.codec;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.Getter;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

@Getter
public class PacketTypeJudger {

  private final DispatcherProperties properties;

  public PacketTypeJudger(DispatcherProperties properties) {
    this.properties = properties;
  }


  public boolean isPrivateMessage(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  public boolean isPrivateMessage(CDTPPacket packet) {
    return this.isPrivateMessage(packet.getCommandSpace(), packet.getCommand());
  }

  public boolean isPrivateMessage(Integer mqMsgEventType) {
    return mqMsgEventType.intValue() == 0;
  }


  public boolean isGroupMessage(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1) &&
        properties.isGroupPacketEnabled();
  }

  public boolean isGroupMessage(CDTPPacket packet) {
    return this.isGroupMessage(packet.getCommandSpace(), packet.getCommand());
  }


  public boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 0x0107;
  }

  public boolean isGroupJoin(CDTPPacket cdtpPacket) {
    return this.isGroupJoin(cdtpPacket.getCommandSpace(), cdtpPacket.getCommand());
  }


  public boolean isPacketDataEncryptedByReceiver(CDTPHeader cdtpHeader) {
    return cdtpHeader.getDataEncryptionMethod() == 4;
  }

}
