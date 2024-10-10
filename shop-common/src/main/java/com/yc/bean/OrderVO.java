package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderVO {
    private Integer id;

    private String province;//地址
    private String city;//地址
    private String town;//地址
    private String street;//地址

    private String mobile;  //手机号
    private String contact; //收货人

    private String distance;//距离
    private String freight;//运费
    private Integer coupon_id;//优惠券id
}
