package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.DispatcherProperties.RocketMQ;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Configuration
public class RocketMqConfiguration {

  @Bean
  public MQConsumer consumer(DispatcherProperties properties, RestTemplate restTemplate, MQProducer producer)
      throws Exception {
    RocketMQ rocketMQ = properties.getRocketmq();
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMQ.getConsumerGroup());
    consumer.setNamesrvAddr(rocketMQ.getNamesrvAddr());
    consumer.subscribe(rocketMQ.getConsumerTopic(), "*");
    consumer.setMessageListener(new DispatchListener(producer, restTemplate, properties.getTemailChannelUrl()));
    consumer.start();
    return consumer;
  }

  @Bean
  public MQProducer producer(DispatcherProperties properties) throws Exception {
    DefaultMQProducer producer = new DefaultMQProducer(properties.getRocketmq().getProducerGroup());
    producer.setNamesrvAddr(properties.getRocketmq().getNamesrvAddr());
    producer.start();
    return producer;
  }
}
