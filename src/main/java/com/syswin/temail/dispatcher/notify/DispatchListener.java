package com.syswin.temail.dispatcher.notify;

import static com.syswin.temail.dispatcher.Constants.CDTP_VERSION;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND;
import static com.syswin.temail.dispatcher.Constants.NOTIFY_COMMAND_SPACE;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountStatus;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountStatusLocateResponse;
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
import org.springframework.web.client.RestTemplate;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Slf4j
public class DispatchListener implements MessageListenerConcurrently {

  private Gson gson = new Gson();
  private MQProducer producer;
  private RestTemplate restTemplate;
  private String temailChannelUrl;

  public DispatchListener(MQProducer producer, RestTemplate restTemplate, String temailChannelUrl) {
    this.producer = producer;
    this.restTemplate = restTemplate;
    this.temailChannelUrl = temailChannelUrl;
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        String msgData = new String(msg.getBody());
        log.info("接收到的消息是：{}", msgData);
        MessageBody messageBody;
        try {
          messageBody = gson.fromJson(msgData, MessageBody.class);
          if (messageBody != null) {
            CDTPPacketTrans.Header header = gson.fromJson(messageBody.getHeader(), CDTPPacketTrans.Header.class);
            if (header != null) {
              String receiver = messageBody.getReceiver();
              List<TemailAccountStatus> statusList = getServerTagsByTemail(receiver);
              if (!statusList.isEmpty()) {
                List<Message> msgList = new ArrayList<>();
                CDTPPacketTrans packet = new CDTPPacketTrans();
                packet.setCommandSpace(NOTIFY_COMMAND_SPACE);
                packet.setCommand(NOTIFY_COMMAND);
                packet.setVersion(CDTP_VERSION);
                packet.setHeader(header);
                packet.setData(gson.toJson(messageBody.getData()));

                byte[] messageData = gson.toJson(packet).getBytes();
                statusList.forEach(status ->
                    msgList.add(new Message(status.getMqTopic(), status.getMqTag(), messageData))
                );
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

  private List<TemailAccountStatus> getServerTagsByTemail(String temail) {
    // 根据temail地址从状态服务器获取该temail对应的通道所在topic
    try {
      log.info("获取请求用户所属通道信息:url={}, temail={}", temailChannelUrl, temail);
      TemailAccountStatusLocateResponse response = restTemplate
          .getForObject(temailChannelUrl, TemailAccountStatusLocateResponse.class, temail);
      if (response != null) {
        List<TemailAccountStatus> statuses = response.getStatusList();
        if (statuses != null && !statuses.isEmpty()) {
          return statuses;
        }
      }
    } catch (Exception e) {
      log.error("获取用户所属通道时出错！", e);
      // 获取用户所属通道时出错时，丢弃推送消息
    }
    return new ArrayList<>();
  }
}
