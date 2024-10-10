package com.yc.api;

import com.yc.bean.Coupon;
import com.yc.model.JsonModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("shop-coupon")
public interface CouponClient {
    @GetMapping("/coupon/checkCoupon")
    public Coupon checkCouponByOrder(@RequestParam("coupon_id") String coupon_id);

    @PutMapping("/coupon/useCoupon")
    public JsonModel useCoupon(@RequestParam("userId") String userId, @RequestParam("couponId") Integer couponId);
}
