package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!dev")
class DispatchMqConfiguration {

  private static final String MQ_TOPIC_TAG = "*";

//  @Bean
//  MqProducerConfig mqProducerConfig(DispatcherProperties properties, RestTemplate restTemplate) {
//    return new MqProducerConfig(properties.getRocketmq().getProducerGroup(), MqImplementation.ROCKET_MQ);
//  }
//
//  @Bean
//  MqConsumerConfig mqConsumerConfig(DispatcherProperties properties, RestTemplate restTemplate, PacketTypeJudge packetTypeJudge,
//      Map<String, RocketMqProducer> mqProducers) {
//    MQMsgSender mqMsgSender = new CommonMQMsgSender(mqProducers.get(properties.getRocketmq().getProducerGroup()));
//    ChannelStsLocator gatewayLocator = new RemoteChannelStsLocator(restTemplate, properties.getTemailChannelUrl());
//    final MessageHandler messageHandler = new MessageHandler(mqMsgSender, gatewayLocator,
//        properties.getRocketmq().getPushTopic(),
//        properties.getRocketmq().getPushTag(), packetTypeJudge);
//    return  MqConsumerConfig.create().implementation(MqImplementation.ROCKET_MQ).listener(str->{
//      try {
//        messageHandler.onMessageReceived(str);
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }).tag(MQ_TOPIC_TAG)
//        .topic(properties.getRocketmq().getConsumerTopic())
//        .group(properties.getRocketmq().getConsumerGroup())
//        .concurrent().type(MqConsumerType.CLUSTER).build();
//  }

  @Bean
  MQConsumer consumer(DispatcherProperties properties, RestTemplate restTemplate, MQProducer producer,
                      PacketTypeJudge packetTypeJudge) throws Exception {
    DispatcherProperties.RocketMQ rocketMQ = properties.getRocketmq();
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMQ.getConsumerGroup());
    consumer.setNamesrvAddr(rocketMQ.getNamesrvAddr());
    consumer.subscribe(rocketMQ.getConsumerTopic(), MQ_TOPIC_TAG);
    consumer.setMessageListener(new RocketDispatchListener(new RocketMQProducer(producer),
        new RemoteChannelStsLocator(restTemplate, properties.getTemailChannelUrl()), properties, packetTypeJudge));
    consumer.start();
    log.info("Started listening to MQ topic {}, tag {}", rocketMQ.getConsumerTopic(), MQ_TOPIC_TAG);
    return consumer;
  }

  @Bean
  MQProducer producer(DispatcherProperties properties) throws Exception {
    DefaultMQProducer producer = new DefaultMQProducer(properties.getRocketmq().getProducerGroup());
    producer.setNamesrvAddr(properties.getRocketmq().getNamesrvAddr());
    producer.start();
    return producer;
  }

}
