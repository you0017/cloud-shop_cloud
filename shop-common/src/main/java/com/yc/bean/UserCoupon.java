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
@TableName("user_coupon")
@Builder
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private int id;
    private int user_id;
    private int coupon_id;
    private Integer used;   //0未使用 1已使用 2已过期
}