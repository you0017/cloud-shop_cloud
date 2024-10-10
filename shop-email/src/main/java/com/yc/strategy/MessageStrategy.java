package com.yc.strategy;


import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;

import java.util.List;

public interface MessageStrategy {

    public String transformation(UserInformation user);
    public String transformation(List<OrderDetail> orderDetails, UserInformation user);
}
