package com.syswin.temail.dispatcher.codec;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import lombok.Getter;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

@Getter
public class PacketTypeJudge {

  private final DispatcherProperties properties;

  public PacketTypeJudge(DispatcherProperties properties) {
    this.properties = properties;
  }


  public boolean isPrivateDecryptType(short commandSpace, short command) {
    return this.isPrivateMessage(commandSpace, command)
        || this.isPrivateMessageReply(commandSpace, command)
        || this.isPrivateMessageReplyToClientSelf(commandSpace, command);
  }

  public boolean isPrivateMessage(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  public boolean isPrivateMessageReplyToClientSelf(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  public boolean isPrivateMessageReply(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 0x1005;
  }


  public boolean isGroupDecryptType(short commandSpace, short command){
    return this.isGroupMessage(commandSpace,command)
        || this.isGroupMessageReply(commandSpace,command);
  }

  public boolean isGroupMessage(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1) &&
        properties.isGroupPacketEnabled();
  }

  public boolean isGroupMessageReply(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 0x010E) &&
        properties.isGroupPacketEnabled();
  }


  public boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 0x0107;
  }

  public boolean isPrivateMessage(Integer mqMsgEventType) {
    return mqMsgEventType.intValue() == 0;
  }

  public boolean isPacketDataEncryptedByReceiver(CDTPHeader cdtpHeader) {
    return cdtpHeader.getDataEncryptionMethod() == 4 ||
        cdtpHeader.getDataEncryptionMethod() == 5;
  }

}
