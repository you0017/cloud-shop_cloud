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
@TableName("order_detail")
public class OrderDetail {
    @TableId(type = IdType.AUTO)
    private Integer id; //
    private Integer order_id;   //订单id
    private Integer item_id;    //商品id
    private Integer num;    //数量
    private String name;   //名字
    private String spec;   //规格
    private Double price;  //价格
    private Double actual_payment; //实际价格
    private String image;  //图片
    private String create_time;
    private String update_time;
    private Integer return_status; //退货状态 1 未退 2待处理 3已退
}
