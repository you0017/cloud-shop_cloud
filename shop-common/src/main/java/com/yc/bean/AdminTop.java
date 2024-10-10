package com.yc.bean;


import lombok.Data;

// 商品销售top表
@Data
public class AdminTop {
    private String name;
    private Integer sold_quantity; // 销售量
    private Double total_sales; // 销售金额

}
