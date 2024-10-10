package com.yc.service;



import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;

import java.util.List;

public interface VelocityTemplateBiz {
    public String genEmailContent(String opType, UserInformation user);
    public String genEmailContent(String opType, List<OrderDetail> orderDetails,UserInformation userInformation);
}
