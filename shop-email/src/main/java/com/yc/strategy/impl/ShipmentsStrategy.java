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

@Component(value = "shipments")
public class ShipmentsStrategy implements MessageStrategy {
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
        return null;
    }

    @Override
    public String transformation(List<OrderDetail> orderDetails, UserInformation user) {
        Date d = new Date();
        //模板上下文，用于存占位符的值
        context.put("username", user.getAccountname());
        context.put("email", user.getEmail());
        context.put("subject","发货");
        context.put("optime",fullDf.format(d));
        context.put("orderId",orderDetails.get(0).getOrder_id());
        context.put("currentDate",partDf.format(d));

        String items = "商品有:";
        for (OrderDetail orderDetail : orderDetails) {
            items += orderDetail.getName()+",";
        }
        items = items.substring(0,items.length()-1);
        context.put("items",items);

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
}
