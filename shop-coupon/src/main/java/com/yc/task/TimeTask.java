package com.yc.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.bean.Coupon;
import com.yc.bean.UserCoupon;
import com.yc.mapper.CouponMapper;
import com.yc.mapper.UserCouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimeTask {
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;

    /**
     * 优惠券过期处理
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void task() {
        //这个是用来算优惠券过期的
        LambdaUpdateWrapper<Coupon> db = new LambdaUpdateWrapper<>();
        db.le(Coupon::getExpiration_date, LocalDate.now()).set(Coupon::getStatus, "expired");
        List<Coupon> coupons = couponMapper.selectList(db);
        couponMapper.update(null, db);

        //这个是用来算用户优惠券过期的
        List<UserCoupon> userCoupons = new ArrayList<>();
        for (Coupon coupon : coupons) {
            UserCoupon build = UserCoupon.builder().used(2).coupon_id(coupon.getId()).build();
            userCoupons.add(build);
        }
        userCouponMapper.updateById(userCoupons);
    }
}
