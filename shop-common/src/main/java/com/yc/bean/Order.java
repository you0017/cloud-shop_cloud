package com.yc.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("shoporder")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Double total_fee;  //总价
    private Integer user_id;    //用户id
    private Integer status;     //订单状态
    private String create_time;  //创建时间
    private String pay_time;     //支付时间
    private String consign_time; //发货时间
    private String end_time; //支付完成时间
    private String close_time;   //交易关闭时间
    private String update_time;  //更新时间
    private String address;//地址
    private String mobile;  //手机号
    private String contact; //收货人
    private String reason;  // 退款理由
    private String send_company;  // 商家发货的物流公司
    private String send_id; // 商家发货的物流单号
    private Double actual_payment; //实际支付价格
    private Integer coupon_id;//优惠券id
    private Double shipping_fee;//运费
}
