package com.syswin.temail.cdtp.dispatcher.notify;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Configuration
public class RocketMqConfiguration {

  @Bean
  public MQConsumer consumer(RocketProperties properties, MQProducer producer) throws Exception {
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
    consumer.setNamesrvAddr(properties.getNamesrvAddr());
    consumer.subscribe(properties.getTopic(), "*");
    consumer.setMessageListener(new DispatchListener(producer));
    consumer.start();
    return consumer;
  }

  @Bean
  public MQProducer producer(RocketProperties properties) throws Exception {
    DefaultMQProducer producer = new DefaultMQProducer(properties.getProducerGroup());
    producer.setNamesrvAddr(properties.getNamesrvAddr());
    producer.start();
    return producer;
  }
}
