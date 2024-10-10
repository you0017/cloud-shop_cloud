package com.yc.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("returnorders")
public class ReturnOrder {
    private Integer return_id; // 退货订单的唯一标识符
    private Integer order_id; // 原始订单的唯一标识符
    private Integer customer_id; // 退货客户的唯一标识符
    private Integer product_id; // 退货产品的唯一标识符
    private String return_reason; // 退货原因的描述
    private Integer return_quantity; // 退货产品的数量
    private String return_status; // 退货状态（例如：申请，申请通过，(给单号)，待处理，退货成功，不予退货)
    private String return_date; // 退货申请日期
    private Double refund_amount; // 退款金额
    private String processing_date; // 退货处理日期
    private String tracking_number;//快递单号
    private String product_name;//商品名
    private String tracking_company;//快递公司
    private String back_type; // 退款类型 1：仅退款 2：退款退货
}
