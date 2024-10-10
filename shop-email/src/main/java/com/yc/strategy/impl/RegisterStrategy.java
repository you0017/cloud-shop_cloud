package com.yc.strategy.impl;


import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;
import com.yc.strategy.MessageStrategy;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

@Component(value = "register")
public class RegisterStrategy implements MessageStrategy {
    @Autowired
    protected VelocityContext context;
    @Autowired
    @Qualifier("registerTemplate")
    private Template orderTemplate;
    @Autowired
    @Qualifier("fullDf")
    private DateFormat fullDf;
    @Autowired
    @Qualifier("partDf")
    private DateFormat partDf;
    @Override
    public String transformation(UserInformation user) {
        Date d = new Date();
        //模板上下文，用于存占位符的值
        context.put("username", user.getAccountname());
        context.put("email", user.getEmail());
        context.put("subject","注册");
        context.put("optime",fullDf.format(d));
        context.put("currentDate",partDf.format(d));

        //合并模板和容器
        //Template template = velocityEngine.getTemplate("vms/deposit.vm","utf-8");

        try(StringWriter writer = new StringWriter()){
            //template.merge(context,writer);//合并内容，替换占位符
            orderTemplate.merge(context,writer);//合并内容，替换占位符
            return writer.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String transformation(List<OrderDetail> orderDetails, UserInformation user) {
        return null;
    }
}
