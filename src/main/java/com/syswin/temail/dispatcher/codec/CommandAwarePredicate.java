package com.syswin.temail.dispatcher.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

import com.syswin.temail.dispatcher.DispatcherProperties;
import java.util.function.BiPredicate;

public class CommandAwarePredicate implements BiPredicate<Short, Short> {

  private final DispatcherProperties properties;

  public CommandAwarePredicate(DispatcherProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean test(Short commandSpace, Short command) {
    return isPrivateMessage(commandSpace, command)
        || isGroupMessage(commandSpace, command);
  }

  private boolean isPrivateMessage(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  private boolean isGroupMessage(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1)
        && properties.isGroupPacketEnabled();
  }

  private boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == 2 && command == 0x0107;
  }
}
