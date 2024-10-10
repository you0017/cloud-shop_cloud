package com.yc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.bean.Coupon;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    public Coupon checkCoupon(String couponCode);

    public void coupon(String couponId);

    public List<Coupon> getCoupons();

    public void useCoupon(String userId, Integer couponId);

    public void grab(Integer couponId);
}
