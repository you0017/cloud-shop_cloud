package com.yc.task;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.Coupon;
import com.yc.mapper.CouponMapper;
import com.yc.utils.YcConstants;
import com.yc.vo.CouponRedis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CouponTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CouponMapper couponMapper;

    //@Scheduled(cron = "* * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    public void task() {
        //获取前六位销量的商品
        List<Coupon> coupons = couponMapper.selectList(null);
        for (Coupon coupon : coupons) {
            CouponRedis couponRedis = new CouponRedis();
            couponRedis.setCouponId(String.valueOf(coupon.getId()));
            couponRedis.setCouponNum(coupon.getAmount());
            //存进redis
            redisTemplate.opsForValue().set(YcConstants.GRAB_COUPON+couponRedis.getCouponId(),couponRedis);
        }
    }
}
