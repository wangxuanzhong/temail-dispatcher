package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND_SPACE;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import com.syswin.temail.dispatcher.request.entity.CDTPPacketTrans;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
class DispatchListener implements MessageListenerConcurrently {

  private final Gson gson = new Gson();
  private final MQProducer producer;
  private final GatewayLocator gatewayLocator;

  DispatchListener(MQProducer producer, GatewayLocator gatewayLocator) {
    this.producer = producer;
    this.gatewayLocator = gatewayLocator;
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        String msgData = new String(msg.getBody());
        log.info("接收到的消息是：{}", msgData);
        try {
          MessageBody messageBody = gson.fromJson(msgData, MessageBody.class);
          if (messageBody != null) {
            CDTPPacketTrans.Header header = gson.fromJson(messageBody.getHeader(), CDTPPacketTrans.Header.class);
            if (header != null) {
              String receiver = messageBody.getReceiver();
              List<TemailAccountLocation> statusList = gatewayLocator.locate(receiver);
              if (!statusList.isEmpty()) {
                String payload = notificationMsgFactory.notificationOf(receiver, header, messageBody.getData());
                byte[] messageData = payload.getBytes();
                List<Message> msgList = new ArrayList<>();
                statusList.forEach(status ->
                    msgList.add(new Message(status.getMqTopic(), status.getMqTag(), messageData))
                );
                log.info("发送消息到gateway {}", statusList);
                producer.send(msgList);
              }
            }
          }
        } catch (JsonSyntaxException e) {
          log.error("消息内容为：{}", msgData);
          log.error("解析错误", e);
          // 不处理，不重试
        }
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.error("队列传输出错！请求参数：" + msgs, e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }
}
