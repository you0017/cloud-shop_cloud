package com.yc.service.impl;



import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;
import com.yc.service.VelocityTemplateBiz;
import com.yc.strategy.impl.StrategyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class VelocityTemplateBizImpl implements VelocityTemplateBiz {

    @Autowired
    private StrategyContext strategyContext;


    @Override
    public String genEmailContent(String opType, UserInformation user) {

        String s = strategyContext.executeStrategy(opType, user);
        return s;
    }

    @Override
    public String genEmailContent(String opType, List<OrderDetail> orderDetails,UserInformation userInformation) {
        String s = strategyContext.executeStrategy(opType, orderDetails,userInformation);
        return s;
    }


}