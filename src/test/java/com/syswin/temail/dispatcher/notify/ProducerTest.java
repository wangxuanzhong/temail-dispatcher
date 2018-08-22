package com.syswin.temail.dispatcher.notify;

import com.google.gson.Gson;
import com.syswin.temail.dispatcher.notify.entity.MessageBody;
import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
public class ProducerTest {

  public static void main(String[] args) throws Exception {
    DefaultMQProducer producer = new DefaultMQProducer("temail-dispatcher-producer-test");
    producer.setNamesrvAddr("172.28.43.18:9876");
    producer.start();
    MessageBody messageBody = new MessageBody();
    Map<String, Object> data = new HashMap<>();
    data.put("key", "这是测试的消息2!");
    messageBody.setData(data);
    messageBody.setToTemail("yaohuacheng@syswin.com");
    Gson gson = new Gson();
    for (int i = 0; i < 10; i++) {
      Message msg = new Message("temail-notify",
          (gson.toJson(messageBody)).getBytes());
      SendResult sendResult = producer.send(msg);
      System.out.println("消息" + sendResult.getOffsetMsgId()
          + "，发送状态" + sendResult.getSendStatus());
    }
    producer.shutdown();
  }
}
