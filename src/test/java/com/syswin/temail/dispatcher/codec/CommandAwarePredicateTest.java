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
import com.syswin.temail.dispatcher.DispatcherProperties;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CommandAwarePredicateTest {

  private final DispatcherProperties properties = new DispatcherProperties();
  private final CommandAwarePredicate predicate = new CommandAwarePredicate(new PacketTypeJudge(null));

  @Test
  public void trueIfPrivateMessage() {
    Assertions.assertThat(predicate.test(SINGLE_MESSAGE_CODE, (short) 1)).isTrue();
  }

  @Test
  public void trueIfGroupMessage() {
    Assertions.assertThat(predicate.test(GROUP_MESSAGE_CODE, (short) 1)).isTrue();
  }

  @Test
  public void falseIfAnyOtherPrivateMessage() {
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != 1 && i != 0x1005 && i != 0x3000) {
        Assertions.assertThat(predicate.test(SINGLE_MESSAGE_CODE, i)).isFalse();
      }
    }
  }

  @Test
  public void falseIfAnyOtherGroupMessage() {
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != 1 && i != 0x010E
          && i != 0x011A && i != 0x011E) {
        Assertions.assertThat(predicate.test(GROUP_MESSAGE_CODE, i)).isFalse();
      }
    }
  }

  @Test
  public void falseIfAnythingElse() {
    final Random random = new Random();
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != SINGLE_MESSAGE_CODE && i != GROUP_MESSAGE_CODE) {
        Assertions.assertThat(predicate.test(i, (short) random.nextInt())).isFalse();
      }
    }
  }
}
