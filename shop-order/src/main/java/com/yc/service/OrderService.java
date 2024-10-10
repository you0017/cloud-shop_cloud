package com.yc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.bean.Order;
import com.yc.bean.OrderDetail;
import com.yc.bean.OrderList;
import com.yc.bean.OrderVO;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    public void cancel(String reason, String id);

    public List<OrderList> getOrder(Integer pageNo, Integer pageSize);

    public Order pay(OrderVO orderVO);

    public boolean ret(String op, String itemId, String orderId, String reason, String trackingCompany, String trackingNumber, String returnId);

    public boolean hasUserPurchasedProduct(String id, String userId);
}
