package com.syswin.temail.dispatcher.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import com.syswin.temail.dispatcher.DispatcherProperties;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CommandAwarePredicateTest {

  private final DispatcherProperties properties = new DispatcherProperties();
  private final CommandAwarePredicate predicate = new CommandAwarePredicate(new PacketTypeJudge(properties));

  @Test
  public void trueIfPrivateMessage() {
    Assertions.assertThat(predicate.test(SINGLE_MESSAGE_CODE, (short) 1)).isTrue();
  }

  @Test
  public void trueIfGroupMessage() {
    properties.setGroupPacketEnabled(true);
    Assertions.assertThat(predicate.test(GROUP_MESSAGE_CODE, (short) 1)).isTrue();
  }

  @Test
  public void falseIfGroupMessageToggledOff() {
    properties.setGroupPacketEnabled(false);
    Assertions.assertThat(predicate.test(GROUP_MESSAGE_CODE, (short) 1)).isFalse();
  }

  @Test
  public void falseIfAnyOtherPrivateMessage() {
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != 1 && i != 0x1005) {
        Assertions.assertThat(predicate.test(SINGLE_MESSAGE_CODE, i)).isFalse();
      }
    }
  }

  @Test
  public void falseIfAnyOtherGroupMessage() {
    properties.setGroupPacketEnabled(true);
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
    properties.setGroupPacketEnabled(true);
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != SINGLE_MESSAGE_CODE && i != GROUP_MESSAGE_CODE) {
        Assertions.assertThat(predicate.test(i, (short) random.nextInt())).isFalse();
      }
    }
  }
}
