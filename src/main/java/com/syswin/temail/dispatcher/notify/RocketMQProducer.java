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

import com.syswin.temail.dispatcher.notify.entity.MqMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.common.message.Message;

public class RocketMQProducer implements MQMsgSender {

  private org.apache.rocketmq.client.producer.MQProducer producer;

  public RocketMQProducer(org.apache.rocketmq.client.producer.MQProducer producer) {
    this.producer = producer;
  }

  @Override
  public void send(List<MqMessage> mqMessages) throws Exception {
    List<Message> messageList = new ArrayList<>(mqMessages.size());
    for (MqMessage msg : mqMessages) {
      messageList.add(new Message(msg.getGatewayFlag(), msg.getInstanceFlag(), msg.getBody().getBytes()));
    }
    producer.send(messageList);
  }
}
