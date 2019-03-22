package com.syswin.temail.dispatcher.codec;

import java.util.function.BiPredicate;

public class CommandAwarePredicate implements BiPredicate<Short, Short> {

  private final PacketTypeJudge packetTypeJudge;

  public CommandAwarePredicate(
      PacketTypeJudge packetTypeJudge) {
    this.packetTypeJudge = packetTypeJudge;
  }

  @Override
  public boolean test(Short commandSpace, Short command) {
    return isPrivateMessage(commandSpace, command)
        || isGroupMessage(commandSpace, command)
        || isNewGroupMessage(commandSpace, command);
  }

  private boolean isPrivateMessage(short commandSpace, short command) {
    return packetTypeJudge.isPrivateDecryptType(commandSpace, command);
  }

  private boolean isGroupMessage(short commandSpace, short command) {
    return packetTypeJudge.isGroupDecryptType(commandSpace, command);
  }

  private boolean isNewGroupMessage(short commandSpace, short command) {
    return packetTypeJudge.isNewGroupMessage(commandSpace, command);
  }

}
