package com.yc.strategy.impl;



import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;
import com.yc.strategy.MessageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StrategyContext {

    private final Map<String, MessageStrategy> messageStrategies;

    public String executeStrategy(String opType, UserInformation user) {
        String info;

        MessageStrategy messageStrategy = messageStrategies.get(opType);
        if (messageStrategy != null) {
            info = messageStrategy.transformation(user);
        } else {
            throw new RuntimeException("未知操作");
        }

        return info;
    }

    public String executeStrategy(String opType, List<OrderDetail> orderDetails,UserInformation userInformation) {
        String info;

        MessageStrategy messageStrategy = messageStrategies.get(opType);
        if (messageStrategy != null) {
            info = messageStrategy.transformation(orderDetails,userInformation);
        } else {
            throw new RuntimeException("未知操作");
        }

        return info;
    }
}
