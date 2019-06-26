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
