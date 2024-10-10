package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderList {
    private Integer id; //订单编号
    private Double total_fee;   //总价
    private Double actual_payment;//实际支付
    private Integer status; //订单状态
    private String create_time; //创建时间
    private String address;//地址
    private List<OrderDetail> orderDetailList;//详细商品
    private String mobile;  //手机号
    private String contact; //收货人
    private Double shipping_fee;//运费
}
