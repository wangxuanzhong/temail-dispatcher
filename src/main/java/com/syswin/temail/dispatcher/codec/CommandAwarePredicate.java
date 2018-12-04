package com.syswin.temail.dispatcher.codec;

import java.util.function.BiPredicate;

public class CommandAwarePredicate implements BiPredicate<Short, Short> {

  private final PacketTypeJudger packetTypeJudger;

  public CommandAwarePredicate(
      PacketTypeJudger packetTypeJudger) {
    this.packetTypeJudger = packetTypeJudger;
  }

  @Override
  public boolean test(Short commandSpace, Short command) {
    return isPrivateMessage(commandSpace, command)
        || isGroupMessage(commandSpace, command);
  }

  private boolean isPrivateMessage(short commandSpace, short command) {
    return packetTypeJudger.isPrivateMessage(commandSpace, command);
  }

  private boolean isGroupMessage(short commandSpace, short command) {
    return packetTypeJudger.isGroupMessage(commandSpace, command);
  }

  private boolean isGroupJoin(short commandSpace, short command) {
    return packetTypeJudger.isGroupJoin(commandSpace, command);
  }

}
