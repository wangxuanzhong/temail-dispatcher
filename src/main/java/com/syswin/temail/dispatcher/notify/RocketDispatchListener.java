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

package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import com.syswin.temail.dispatcher.notify.suspicious.TaskExecutor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
public class RocketDispatchListener implements MessageListenerConcurrently {

  private final MessageHandler messageHandler;

  public RocketDispatchListener(MQMsgSender producer, ChannelStsLocator gatewayLocator,
      DispatcherProperties properties, PacketTypeJudge packetTypeJudge, TaskExecutor taskExecutor,
      NotificationMessageFactory notificationMessageFactory) {
    messageHandler = new MessageHandler(producer, gatewayLocator, properties.getRocketmq().getPushTopic(),
        properties.getRocketmq().getPushTag(), packetTypeJudge, taskExecutor::offer, notificationMessageFactory);
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        messageHandler.onMessageReceived(new String(msg.getBody()));
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.error("fail to send message!  param: {}", msgs, e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

}
