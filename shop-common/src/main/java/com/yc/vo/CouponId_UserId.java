package com.yc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用于activemq传递
 */
public class CouponId_UserId {
    private Integer couponId;
    private String userId;
}
