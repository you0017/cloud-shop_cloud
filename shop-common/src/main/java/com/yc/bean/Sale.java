package com.yc.bean;

import lombok.Data;

import java.io.Serializable;

// 销售记录
@Data
public class Sale implements Serializable {
    private String name;    // 商品名称
    private Double price;    // 价格
    private Integer sold;   // 销量
    private String total_price;  // 总销售金额
    private String create_time;    // 上架日期

}
