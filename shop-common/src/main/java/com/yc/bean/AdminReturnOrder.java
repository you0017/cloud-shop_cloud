package com.yc.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("returnorders")
public class AdminReturnOrder {
    private Integer return_id; // 退货单号
    private String return_type; // 售后类型 1、退货 2、退款
    private String tracking_number; // 快递单号
    private String order_id; // 订单号
    private String product_name; // 产品名称
    private Double refund_amount; // 退款金额
    private String name; // 用户名
    private String return_date; // 申请时间
    private String return_status; // 状态 1、待处理  2、审核通过
    private String back_type; // 类型  退款类型 1：仅退款 2：退款退货
    private String return_reason; // 原因
    private Integer return_quantity; // 退货数量
    private Integer product_id; // 产品id
    private String tracking_company; // 快递公司
}
