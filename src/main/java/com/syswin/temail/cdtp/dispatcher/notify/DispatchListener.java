package com.syswin.temail.cdtp.dispatcher.notify;

import static com.syswin.temail.cdtp.dispatcher.Constants.NOTIFY_COMMAND;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.cdtp.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.cdtp.dispatcher.notify.entity.TemailAccountStatusLocateResponse;
import com.syswin.temail.cdtp.dispatcher.notify.entity.TemailAccountStatusLocateResponse.TemailAccountStatus;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPHeader;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
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
  private String cdtpStatusUrl;
  private String producerTopic;

  public DispatchListener(MQProducer producer, RestTemplate restTemplate, String cdtpStatusUrl,
      String producerTopic) {
    this.producer = producer;
    this.restTemplate = restTemplate;
    this.cdtpStatusUrl = cdtpStatusUrl;
    this.producerTopic = producerTopic;
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
          CDTPHeader header = gson.fromJson(messageBody.getHeader(), CDTPHeader.class);
          String toTemail = messageBody.getToTemail();
          header.setCommand(NOTIFY_COMMAND);
          header.setTo(toTemail);
          CDTPPackage cdtpPackage = new CDTPPackage(header);
          cdtpPackage.setData(gson.toJson(messageBody.getData()));
          byte[] messageData = gson.toJson(cdtpPackage).getBytes();

          List<String> topics = getServerTagsByTemail(toTemail);
          List<Message> msgList = new ArrayList<>();
          topics.forEach(serverTag -> msgList.add(new Message(producerTopic, serverTag, messageData)));
          producer.send(msgList);
        } catch (JsonSyntaxException e) {
          log.error("消息内容为：{}", msgData);
          log.error("解析错误", e);
          // 不处理
        }
      }

      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.error("队列传输出错！请求参数：" + msgs, e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

  private List<String> getServerTagsByTemail(String temail) {
    // 根据temail地址从状态服务器获取该temail对应的通道所在topic
    List<String> tags = new ArrayList<>();
    TemailAccountStatusLocateResponse response = restTemplate
        .getForObject(cdtpStatusUrl, TemailAccountStatusLocateResponse.class, temail);
    if (response != null) {
      List<TemailAccountStatus> statusList = response.getStatusList();
      if (statusList != null && !statusList.isEmpty()) {
        for (TemailAccountStatus temailAccountStatus : statusList) {
          tags.add(temailAccountStatus.getMqTopic());
        }
      }
    }
    return tags;
  }
}
