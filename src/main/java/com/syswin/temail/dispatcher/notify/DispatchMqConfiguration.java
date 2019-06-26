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
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@Profile("!dev")
class DispatchMqConfiguration {

  private static final String MQ_TOPIC_TAG = "*";

  @Bean
  MQConsumer consumer(DispatcherProperties properties, RestTemplate restTemplate,
      MQProducer producer, PacketTypeJudge packetTypeJudge, TaskExecutor taskExecutor,
      NotificationMessageFactory notificationMessageFactory) throws Exception {

    DispatcherProperties.RocketMQ rocketMQ = properties.getRocketmq();
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMQ.getConsumerGroup());
    consumer.setNamesrvAddr(rocketMQ.getNamesrvAddr());
    consumer.subscribe(rocketMQ.getConsumerTopic(), MQ_TOPIC_TAG);
    consumer.setMessageListener(new RocketDispatchListener(new RocketMQProducer(producer),
        new RemoteChannelStsLocator(restTemplate, properties.getTemailChannelUrl()),
        properties, packetTypeJudge, taskExecutor, notificationMessageFactory));
    consumer.start();
    log.info("Started listening to MQ topic {}, tag {}", rocketMQ.getConsumerTopic(), MQ_TOPIC_TAG);
    return consumer;
  }

  @Bean
  MQProducer producer(DispatcherProperties properties) throws Exception {
    log.info("get rocket group is {}.", properties.getRocketmq().getProducerGroup());
    DefaultMQProducer producer = new DefaultMQProducer(properties.getRocketmq().getProducerGroup());
    producer.setNamesrvAddr(properties.getRocketmq().getNamesrvAddr());
    producer.start();
    return producer;
  }

}
