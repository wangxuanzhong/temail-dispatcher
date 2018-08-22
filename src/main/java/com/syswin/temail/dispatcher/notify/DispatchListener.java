package com.syswin.temail.dispatcher.notify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.dispatcher.Constants;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountStatus;
import com.syswin.temail.dispatcher.notify.entity.TemailAccountStatusLocateResponse;
import com.syswin.temail.dispatcher.request.entity.CDTPHeader;
import com.syswin.temail.dispatcher.request.entity.CDTPPackage;
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
            CDTPHeader header = gson.fromJson(messageBody.getHeader(), CDTPHeader.class);
            if (header != null) {
              String toTemail = messageBody.getToTemail();
              header.setCommand(Constants.NOTIFY_COMMAND);
              header.setTo(toTemail);
              CDTPPackage cdtpPackage = new CDTPPackage(header);
              cdtpPackage.setData(gson.toJson(messageBody.getData()));
              byte[] messageData = gson.toJson(cdtpPackage).getBytes();

              List<TemailAccountStatus> statusList = getServerTagsByTemail(toTemail);
              if (!statusList.isEmpty()) {
                List<Message> msgList = new ArrayList<>();
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
          // 不处理
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
    log.info("获取请求用户所属通道信息:url={}, temail={}", temailChannelUrl, temail);
    TemailAccountStatusLocateResponse response = restTemplate
        .getForObject(temailChannelUrl, TemailAccountStatusLocateResponse.class, temail);
    if (response != null) {
      List<TemailAccountStatus> statuses = response.getStatusList();
      if (statuses != null && !statuses.isEmpty()) {
        return statuses;
      }
    }
    return new ArrayList<>();
  }
}
