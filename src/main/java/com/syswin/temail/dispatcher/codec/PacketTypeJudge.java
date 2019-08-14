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

package com.syswin.temail.dispatcher.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

import com.syswin.temail.dispatcher.Constants;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.util.Arrays;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class PacketTypeJudge {

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
        || this.isSendAssignedUserReplyMessage(commandSpace, command)
        || this.isCrowdMsg(commandSpace, command);
  }

  public boolean isNewGroupMessage(short commandSpace, short command) {
    return (commandSpace == SINGLE_MESSAGE_CODE && command == 0x3000);
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
    String offPushType = dispatcherProperties.getOffPushType();
    return !StringUtils.isEmpty(offPushType) &&
        Arrays.asList(offPushType.split(",")).contains(eventType.toString());
  }

  public boolean isSenderEqualsToRecevier(CDTPHeader cdtpHeader) {
    return cdtpHeader.getSender() != null &&
        cdtpHeader.getSender().equals(cdtpHeader.getReceiver());
  }

  public boolean isBizServerValidType(short commandSpace) {
    return (commandSpace == GROUP_MESSAGE_CODE) || (commandSpace == 0x000F);
  }

  public boolean isCrowdMsg(short commandSpace, short command) {
    return commandSpace == 0x000F && (command == 1 || command == 0x010E || command == 0x011A || command == 0x011E);
  }
}
