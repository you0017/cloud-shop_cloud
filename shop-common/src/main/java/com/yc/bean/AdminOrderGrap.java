package com.yc.bean;


import lombok.Data;

// 订单的图表
@Data
public class AdminOrderGrap {
    private String order_time;   // 时间
    private Integer order_sum;  // 订单数量
    private Double order_sales;  // 销售额
}
