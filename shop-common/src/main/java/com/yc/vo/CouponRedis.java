package com.yc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponRedis {
    private String couponId;//券id
    private Integer couponNum;//券数量
}
