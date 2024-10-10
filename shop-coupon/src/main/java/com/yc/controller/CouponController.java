package com.yc.controller;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.bean.Coupon;
import com.yc.bean.UserCoupon;
import com.yc.context.BaseContext;
import com.yc.mapper.CouponMapper;
import com.yc.mapper.UserCouponMapper;
import com.yc.model.JsonModel;
import com.yc.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Random;

//@WebServlet("/html/coupon.action")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponMapper couponMapper;
    /**
     * 检查优惠券是否可用
     * 下单的时候
     */
    @PostMapping("/checkCoupon")
    public JsonModel checkCoupon(@RequestParam("coupon_code") String couponCode) {
        Coupon coupon = couponService.checkCoupon(couponCode);

        return JsonModel.ok().setDate(coupon);
    }
    @GetMapping("/checkCoupon")
    public Coupon checkCouponByOrder(@RequestParam("coupon_id") String coupon_id) {
        Coupon coupon = couponService.checkCoupon(couponMapper.selectById(coupon_id).getCode());

        return coupon;
    }

    /**
     * 领券
     */
    @PostMapping("/coupon")
    public JsonModel coupon(@RequestParam("coupon_id") String coupon_id) {
        //优惠券id

        couponService.coupon(coupon_id);

        return JsonModel.ok();
    }

    /**
     * 首页展示所有的优惠券
     * 只有领取过的(使用，未使用)
     * 可以抢的
     */
    @GetMapping("/getCoupons")
    public JsonModel getCoupons() {
        List<Coupon> select = couponService.getCoupons();

        return JsonModel.ok().setDate(select);
    }

    /**
     * 优惠券使用
     */
    @PutMapping("/useCoupon")
    public JsonModel useCoupon(String userId, Integer couponId){
        couponService.useCoupon(userId, couponId);
        return JsonModel.ok();
    }

    /**
     * 抢券
     */
    @PostMapping("/grab")
    public JsonModel grab(@RequestParam("coupon_id") Integer couponId){

        //测试用的随机用户id
        /*Random random = new Random();
        //11 -- 25
        Integer result = random.nextInt(15) + 11;
        BaseContext.setCurrentId(String.valueOf(result));*/


        couponService.grab(couponId);
        return JsonModel.ok();
    }
}
