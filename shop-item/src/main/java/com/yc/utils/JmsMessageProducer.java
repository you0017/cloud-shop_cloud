package com.yc.utils;

import com.google.gson.Gson;
import com.yc.bean.UserInformation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class JmsMessageProducer {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Value("early_warning_information")
    private String queueName;

    @Async//线程池异步
    public void warningMessage(String str, String qqMail) {
        //String message = messageBean.toString();
        //将bean转成一个json字符串，序列化后存到activeMQ
        String message = str + "^*^"+qqMail;
        log.info("发送邮件消息：" + message);
        jmsTemplate.convertAndSend(queueName,message);//发消息到队列
        log.info("发送邮件消息成功");
    }
}
