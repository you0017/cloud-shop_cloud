package com.yc.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yc.api.OrderClient;
import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;
import com.yc.service.VelocityTemplateBiz;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@Log4j2
public class JmsMessageConsumer {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private VelocityTemplateBiz velocityTemplateBiz;
    @Autowired
    private MailBiz mailBiz;
    @Autowired
    private OrderClient orderClient;

    /**
     * 注册
     * @param message
     */
    @JmsListener(destination = "register")//监听myQueue消息队列
    public void registerMessage(String message){
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            log.error("线程休眠异常",e);
        }
        System.out.println("接收到消息：" + message);
        Gson gson = new Gson();
        UserInformation user = gson.fromJson(message, UserInformation.class);

        //产生要发送的邮件内容
        String context = velocityTemplateBiz.genEmailContent("register", user);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mailBiz.sendMail(user.getEmail(),"注册通知",context);
    }

    /**
     * 预警
     * @param message
     */
    @JmsListener(destination = "early_warning_information")//监听myQueue消息队列
    public void warningMessage(String message){
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            log.error("线程休眠异常",e);
        }
        System.out.println("接收到消息：" + message);
        String str = message.split("^*^")[0];
        String email = message.split("^*^")[1];

        mailBiz.sendMail(email,"预警通知",str);
    }

    /**
     * 发货
     * @param message
     */
    @JmsListener(destination = "shipments")//监听myQueue消息队列
    public void shipments(String message){
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            log.error("线程休眠异常",e);
        }
        System.out.println("接收到消息：" + message);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> orderIds = gson.fromJson(message, listType);

        for (Integer orderId : orderIds) {
            UserInformation userInformation = orderClient.getUserInformationByOrderId(orderId);
            List<OrderDetail> orderDetails = orderClient.getOrderDetailsByOrderId(orderId);

            String str = velocityTemplateBiz.genEmailContent("shipments", orderDetails,userInformation);
            mailBiz.sendMail(userInformation.getEmail(),"发货通知",str);
        }

    }
}
