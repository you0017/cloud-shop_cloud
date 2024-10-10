package com.yc.bean;

import lombok.Data;


/**
 * 图表销售数据
 CSa*/
@Data
public class GraphSale {
    private String category;   // 种类的名称
    private Integer total_sales;   // 销售量
    private Double total_revenue; // 销售额
}
