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
