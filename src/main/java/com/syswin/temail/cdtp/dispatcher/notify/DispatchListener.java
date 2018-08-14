package com.syswin.temail.cdtp.dispatcher.notify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.util.StringUtils;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Slf4j
public class DispatchListener implements MessageListenerConcurrently {

  private Gson gson = new Gson();
  private MQProducer producer;

  public DispatchListener(MQProducer producer) {
    this.producer = producer;
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        String msgData = new String(msg.getBody());
        log.debug("接收到的消息是：{}", msgData);
        PushMsgBody pushMsgBody;
        try {
          pushMsgBody = gson.fromJson(msgData, PushMsgBody.class);
          String topic = getTopicByTemail(pushMsgBody.getToTemail());
          if (StringUtils.hasText(topic)) {
            Message sendMsg = new Message(topic, pushMsgBody.getData().getBytes());
            producer.send(sendMsg);
          }
        } catch (JsonSyntaxException e) {
          // 不处理
        }
      }

      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.error("队列传输出错！请求参数：" + msgs, e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

  private String getTopicByTemail(String temail) {
    // 根据temail地址从状态服务器获取该temail对应的通道所在topic
    return "defaultChannel";
  }
}
