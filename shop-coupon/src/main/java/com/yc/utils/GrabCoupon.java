package com.yc.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.Coupon;
import com.yc.bean.UserCoupon;
import com.yc.context.BaseContext;
import com.yc.mapper.CouponMapper;
import com.yc.mapper.UserCouponMapper;
import com.yc.vo.CouponId_UserId;
import com.yc.vo.CouponRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GrabCoupon {
    private final RedisLockUtil redisLockUtil;
    private final RedisTemplate redisTemplate;
    private final WebSocket webSocket;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Async
    public void grab(String userId, Integer couponId) {
        log.info("当前是用户id:"+userId);


        String lockKey = YcConstants.COUPON_LOCK+couponId; // 锁的唯一标识
        String requestId = String.valueOf(Thread.currentThread().getId()); // 使用线程 ID 作为唯一标识

        // 尝试获取锁
        boolean locked = redisLockUtil.tryLock(lockKey, requestId, 10);
        if (!locked) {
            log.info("当前系统繁忙");
            webSocket.sendToAllClient(userId,"当前系统繁忙");
            return; // 未获取到锁，返回失败
        }

        try {
            log.info(userId+"抢到了锁");
            // 获取券并进行抢兑
            CouponRedis o = (CouponRedis) redisTemplate.opsForValue().get(YcConstants.GRAB_COUPON + couponId);
            Integer couponNum = o.getCouponNum();

            if (couponNum<=0) {
                log.info("券已被抢兑");
                redisTemplate.delete(YcConstants.GRAB_COUPON + couponId);//这个redis里面的删掉
                return; // 券已被抢兑
            }

            //并且redis双向标志优惠券和用户
            List<String> items = redisTemplate.opsForList().range(YcConstants.USER_COUPON_GRAB + userId, 0,1);
            if (!items.contains(couponId)) {
                //没领过
                redisTemplate.opsForList().rightPush(YcConstants.USER_COUPON_GRAB + userId, couponId);
                redisTemplate.opsForList().rightPush(YcConstants.GRAB_COUPON_USER + couponId, userId);
                log.info("抢到了");
                webSocket.sendToAllClient(userId,"抢到了");

                // 标记为已被抢兑
                //减数量
                couponNum--;
                o.setCouponNum(couponNum);
                redisTemplate.opsForValue().set(YcConstants.GRAB_COUPON + couponId, o);
            } else {
                log.info("你已经领取过了");
                webSocket.sendToAllClient(userId,"你已经领取过了");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 释放锁
            redisLockUtil.unlock(lockKey, requestId);

            //再拿一次
            CouponRedis o = (CouponRedis) redisTemplate.opsForValue().get(YcConstants.GRAB_COUPON + couponId);
            Integer couponNum = o.getCouponNum();

            if (couponNum<=0) {
                log.info("此时券数为0");
                redisTemplate.delete(YcConstants.GRAB_COUPON + couponId);//这个redis里面的删掉

                //并且redis数据存入redis
                //默认已经抢完了，这时候把这个id的所有在redis的用户_用户id写入数据库
                redisToMysql(couponId,userId);
                return; // 券已被抢完
            }
        }
    }


    /**
     * 公共方法写入数据库
     * @param couponId
     * @param userId
     */
    public void redisToMysql(Integer couponId, String userId){
        //这里也要加锁，不然数据库操作时间长了，会导致这个判断依然为true
        boolean tryLock = true;
        if (redisTemplate.hasKey(YcConstants.GRAB_COUPON_USER + couponId)) {
            tryLock = redisLockUtil.tryLock(YcConstants.COUPON_LOCK_SQL + couponId, userId, 10);
        } else {
            //既然已经拿不到锁了，那说明已经抢完了
            webSocket.sendToAllClient(BaseContext.getCurrentId(),"优惠券已经没了");
            return;
        }
        if (tryLock) {

            try {


                //把所有的都存进数据库了
                List<String> ids = redisTemplate.opsForList().range(YcConstants.GRAB_COUPON_USER + couponId, 0, -1);
                //拿了所有的用户id
                List<UserCoupon> userCoupons = new ArrayList<>();
                for (String id : ids) {
                    UserCoupon userCoupon = UserCoupon.builder()
                            .user_id(Integer.parseInt(id))
                            .coupon_id(couponId)
                            .used(0)
                            .build();
                    userCoupons.add(userCoupon);
                }

                userCouponMapper.insert(userCoupons);

                Coupon build = Coupon.builder().id(couponId).amount(0).build();
                couponMapper.updateById(build);

                //删除这个优惠券  --  用户
                redisTemplate.delete(YcConstants.GRAB_COUPON_USER + couponId);
                //删除用户 -- 优惠券  的关于这个优惠券id
                for (String id : ids) {
                    redisTemplate.opsForList().remove(YcConstants.USER_COUPON_GRAB + id, 0, couponId);
                }
            } finally {
                redisLockUtil.unlock(YcConstants.COUPON_LOCK_SQL + couponId, userId);
            }
        } else {
            webSocket.sendToAllClient(BaseContext.getCurrentId(),"优惠券已经没了");
        }
    }
}
