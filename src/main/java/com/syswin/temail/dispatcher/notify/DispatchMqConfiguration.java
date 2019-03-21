package com.syswin.temail.dispatcher.notify;

import com.syswin.library.messaging.all.spring.MqConsumerConfig;
import com.syswin.library.messaging.all.spring.MqConsumerType;
import com.syswin.library.messaging.all.spring.MqImplementation;
import com.syswin.library.messaging.all.spring.MqProducerConfig;
import com.syswin.library.messaging.rocketmq.RocketMqProducer;
import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.codec.PacketTypeJudge;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@Profile("!dev")
class DispatchMqConfiguration {

  private static final String MQ_TOPIC_TAG = "*";

  @Autowired
  private Map<String, RocketMqProducer> mqProducers;

  @Bean
  MqProducerConfig mqProducerConfig(DispatcherProperties properties, RestTemplate restTemplate) {
    return new MqProducerConfig(properties.getRocketmq().getProducerGroup(), MqImplementation.ROCKET_MQ);
  }

  @Bean
  MqConsumerConfig mqConsumerConfig(DispatcherProperties properties, RestTemplate restTemplate, PacketTypeJudge packetTypeJudge) {
    MQMsgSender mqMsgSender = new CommonMQMsgSender(mqProducers.get(properties.getRocketmq().getProducerGroup()));
    GatewayLocator gatewayLocator = new GatewayLocator(restTemplate, properties.getTemailChannelUrl());
    final MessageHandler messageHandler = new MessageHandler(mqMsgSender, gatewayLocator,
        properties.getRocketmq().getPushTopic(),
        properties.getRocketmq().getPushTag(), packetTypeJudge);
    return  MqConsumerConfig.create().implementation(MqImplementation.ROCKET_MQ).listener(str->{
      try {
        messageHandler.onMessageReceived(str);
      } catch (Exception e) {

        e.printStackTrace();
      }
    }).tag(MQ_TOPIC_TAG)
        .topic(properties.getRocketmq().getConsumerTopic())
        .group(properties.getRocketmq().getConsumerGroup())
        .concurrent().type(MqConsumerType.CLUSTER).build();
  }

  //@Bean
  //MQConsumer consumer(DispatcherProperties properties, RestTemplate restTemplate, MQProducer producer,
  //    PacketTypeJudge packetTypeJudge) throws Exception {
  //  RocketMQ rocketMQ = properties.getRocketmq();
  //  DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMQ.getConsumerGroup());
  //  consumer.setNamesrvAddr(rocketMQ.getNamesrvAddr());
  //  consumer.subscribe(rocketMQ.getConsumerTopic(), MQ_TOPIC_TAG);
  //  consumer.setMessageListener(new RocketDispatchListener(new RocketMQProducer(producer),
  //      new GatewayLocator(restTemplate, properties.getTemailChannelUrl()), properties, packetTypeJudge));
  //  consumer.start();
  //  log.info("Started listening to MQ topic {}, tag {}", rocketMQ.getConsumerTopic(), MQ_TOPIC_TAG);
  //  return consumer;
  //}
  //
  //@Bean
  //MQProducer producer(DispatcherProperties properties) throws Exception {
  //  DefaultMQProducer producer = new DefaultMQProducer(properties.getRocketmq().getProducerGroup());
  //  producer.setNamesrvAddr(properties.getRocketmq().getNamesrvAddr());
  //  producer.start();
  //  return producer;
  //}

}
