package com.syswin.temail.dispatcher.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import com.syswin.temail.dispatcher.Constants;
import com.syswin.temail.dispatcher.DispatcherProperties;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class PacketTypeJudge {

  private final String skipedKey = "skipVlalid";

  private final String receiverVlalidKey = "receiverVlalid";

  private DispatcherProperties dispatcherProperties;

  public PacketTypeJudge(DispatcherProperties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
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


  public boolean isGroupDecryptType(short commandSpace, short command) {
    return this.isGroupMessage(commandSpace, command)
        || this.isGroupMessageReply(commandSpace, command)
        || this.isAssignedUserMessageBuild(commandSpace, command)
        || this.isSendAssignedUserReplyMessage(commandSpace, command);
  }

  public boolean isGroupMessage(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1);
  }

  public boolean isGroupMessageReply(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 0x010E);
  }

  public boolean isAssignedUserMessageBuild(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 0x011A;
  }

  public boolean isSendAssignedUserReplyMessage(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 0x011E;
  }


  public boolean isToBePushedMsg(Integer eventType) {
    return eventType.equals(Constants.COMMON_MSG_EVENT_TYPE)
        || eventType.equals(Constants.NOTRACE_MSG_EVENT_TYPE);
  }


  public boolean isBizServerValidType(short commandSpace) {
    return (commandSpace == GROUP_MESSAGE_CODE);
  }


  public boolean isVerifyRecieverType(short commandSpace, short command) {
    String existEle = buildToCmdStr(commandSpace, command);
    List<String> verifyRecivers = this.dispatcherProperties.getValidStrategy().get(this.receiverVlalidKey);
    return verifyRecivers != null && (verifyRecivers.contains(existEle.toUpperCase())
        || verifyRecivers.contains(existEle.toLowerCase()));
  }


  public boolean isVerifySkipped(short commandSpace, short command) {
    String existEle = buildToCmdStr(commandSpace, command);
    List<String> verifyRecivers = this.dispatcherProperties.getValidStrategy().get(this.getSkipedKey());
    return verifyRecivers != null && (verifyRecivers.contains(existEle.toUpperCase())
        || verifyRecivers.contains(existEle.toLowerCase()));
  }

  private String buildToCmdStr(short commandSpace, short command) {
    return StringUtils.leftPad(Integer.toHexString(commandSpace), 4, "0") + "-" +
        StringUtils.leftPad(Integer.toHexString(command), 4, "0");
  }


}
