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

package com.syswin.temail.dispatcher.request.exceptions;

import com.syswin.temail.dispatcher.request.PacketMaker;
import com.syswin.temail.dispatcher.request.application.RequestFactory;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DispatchExceptionTest {

  private final CDTPPacket cdtpPacket = PacketMaker.loginPacket("sean", "deviceId");

  private final DispatchException dispatchException = new DispatchException(
      RequestFactory.UNSUPPORTED_CMD_PREfIX + "any error", cdtpPacket);

  @Test
  public void exceptionMsgStartsWithSpecifiedMsg() {
    Assertions.assertThat(dispatchException.getMessage())
        .startsWith(RequestFactory.UNSUPPORTED_CMD_PREfIX);
  }

  @Test
  public void wontCatchAgainWhenThrowInPreviousCatch() {
    Helper helper = new Helper().invoke(true);
    boolean isFinalCatched = helper.isFinalCatched();
    boolean isDispatchExceptionJudged = helper.isDispatchExceptionJudged();
    Assertions.assertThat(helper).isNotNull();
    Assertions.assertThat(isDispatchExceptionJudged).isTrue();
    Assertions.assertThat(isFinalCatched).isFalse();
  }

  private void throwDispatchException(boolean isDispatchException) {
    if (isDispatchException) {
      throw dispatchException;
    } else {
      throw new RuntimeException("any error");
    }
  }

  @Data
  private class Helper {

    private boolean isFinalCatched;
    private boolean isDispatchExceptionJudged;

    public boolean isFinalCatched() {
      return isFinalCatched;
    }

    public boolean isDispatchExceptionJudged() {
      return isDispatchExceptionJudged;
    }

    public Helper invoke(boolean isThrowDispatchException) {
      isFinalCatched = false;
      isDispatchExceptionJudged = false;
      try {
        throwDispatchException(isThrowDispatchException);
      } catch (DispatchException e) {
        if (e.getMessage().startsWith(
            RequestFactory.UNSUPPORTED_CMD_PREfIX)) {
          isDispatchExceptionJudged = true;
          return this;
        } else {
          throw e;
        }
      } catch (Exception e) {
        isFinalCatched = true;
        e.printStackTrace();
      }
      return null;
    }
  }

}