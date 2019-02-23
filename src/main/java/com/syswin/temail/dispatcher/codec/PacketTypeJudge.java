package com.syswin.temail.dispatcher.codec;

    import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
    import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
    import com.syswin.temail.dispatcher.Constants;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import lombok.Getter;

@Getter
public class PacketTypeJudge {

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

  public boolean isGroupType(short commandSpace) {
    return (commandSpace == GROUP_MESSAGE_CODE);
  }

  public boolean isGroupMessage(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1);
  }

  public boolean isGroupMessageReply(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 0x010E);
  }

  public boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 0x0107;
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

  private Map<String, List<String>> verifyReceiverMap = new HashMap<>();

  public PacketTypeJudge() {
    List<String> singleCmds = new ArrayList<>();
    singleCmds.add("1");
    singleCmds.add("5");
    singleCmds.add("6");
    singleCmds.add("1005");
    singleCmds.add("1006");
    singleCmds.add("100B");
    verifyReceiverMap.put(Integer.toHexString(SINGLE_MESSAGE_CODE), singleCmds);

    List<String> topicCmds = new ArrayList<>();
    topicCmds.add("1");
    topicCmds.add("2");
    topicCmds.add("4");
    verifyReceiverMap.put("E", topicCmds);
    this.verifyReceiverMap = verifyReceiverMap;
  }

  public boolean isToBeVerifyRecieverTemail(short commandSpace, short command) {
    return this.verifyReceiverMap.containsKey((Integer.toHexString(commandSpace)).toUpperCase())
        && this.verifyReceiverMap.get((Integer.toHexString(commandSpace)).toUpperCase())
        .contains((Integer.toHexString(command)).toUpperCase());
  }
}
