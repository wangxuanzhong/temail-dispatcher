package com.syswin.temail.cdtp.dispatcher.push;

import com.google.gson.Gson;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
public class ProducerTest {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("cdtp-dispatcher-producer-test");
        producer.setNamesrvAddr("172.28.43.18:9876");
        producer.start();
        PushMsgBody pushMsgBody = new PushMsgBody();
        pushMsgBody.setData("这是测试的消息2!");
        pushMsgBody.setToTemail("yaohuacheng@syswin.com");
        Gson gson = new Gson();
        for (int i = 0; i < 10; i++) {
            Message msg = new Message("cdtp-push",
                    (gson.toJson(pushMsgBody)).getBytes());
            SendResult sendResult = producer.send(msg);
            System.out.println("消息" + sendResult.getOffsetMsgId()
                    + "，发送状态" + sendResult.getSendStatus());
        }
        producer.shutdown();
    }
}
