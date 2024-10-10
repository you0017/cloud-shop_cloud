package com.yc.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.api.ItemClient;
import com.yc.bean.Coupon;
import com.yc.bean.Item;
import com.yc.bean.UserCoupon;
import com.yc.context.BaseContext;
import com.yc.mapper.CouponMapper;
import com.yc.mapper.UserCouponMapper;
import com.yc.service.CouponService;
import com.yc.utils.GrabCoupon;
import com.yc.utils.RedisLockUtil;
import com.yc.utils.WebSocket;
import com.yc.utils.YcConstants;
import com.yc.vo.CouponRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {
    private final UserCouponMapper userCouponMapper;
    private final RedisTemplate redisTemplate;
    private final ItemClient itemClient;
    private final GrabCoupon grabCoupon;

    @Override
    public Coupon checkCoupon(String couponCode) {
        String user_id = BaseContext.getCurrentId();


        //先看看这个优惠券是否存在
        Coupon coupon = this.lambdaQuery().eq(Coupon::getCode, couponCode)
                .eq(Coupon::getStatus, "active")
                .one();
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        //此时优先看redis里面有没有数据，有的话就先把redis的存入mysql，然后在进行判断
        List<Integer> list = redisTemplate.opsForList().range(YcConstants.USER_COUPON_GRAB+user_id, 0,-1);

        boolean flag = true;
        for (Integer i : list) {
            if (i.equals(coupon.getId())){
                flag = true;
                break;
            }
            flag = false;
        }
        if (flag){
            //此时说明当前验证的券是包含在抢的券中
            //把这个券写入数据库再说
            grabCoupon.redisToMysql(coupon.getId(), user_id);
        }

        //看看这个优惠券是否领取过
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUser_id, user_id)
                .eq(UserCoupon::getCoupon_id, coupon.getId());
        UserCoupon userCoupon = userCouponMapper.selectOne(wrapper);
        if (userCoupon == null) {
            throw new RuntimeException("无效优惠券");
        }

        //如果领了
        //看看这个优惠券是否使用过
        if (userCoupon.getUsed() == 1) {
            throw new RuntimeException("优惠券已经使用过了");
        }
        if (userCoupon.getUsed() == 2) {
            throw new RuntimeException("优惠券已经过期");
        }

        //判断这个优惠券的条件是否满足
        //商品id -- 数量
        Map<String, String> map = redisTemplate.opsForHash().entries(YcConstants.CARTITEMS + user_id);

        //取出map里面所有的键
        List<String> ids = new ArrayList<>(map.keySet());
        List<Item> items = itemClient.getByIds(ids);//订单所有商品

        //这里面我只需要单价
        Double sum = 0.0;
        for (Item item : items) {
            String num = map.get(item.getId());
            sum += item.getPrice() * Integer.valueOf(num);//这是总价
        }

        //目前我的优惠券格式是  >xxx
        Double v = Double.valueOf(coupon.getUsage_limit().substring(1));
        if (sum > v) {
            //代表可用
            return coupon;
        } else {
            //不可用
            throw new RuntimeException("优惠券不可用");
        }
    }

    @Override
    public void coupon(String couponId) {
        String user_id = BaseContext.getCurrentId();
        Coupon coupon = this.lambdaQuery()
                .eq(Coupon::getId, couponId)
                .eq(Coupon::getStatus, "active").one();

        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        //这个优惠券存在

        //看看是否领过了
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUser_id, user_id)
                .eq(UserCoupon::getCoupon_id, couponId);
        UserCoupon userCoupon = userCouponMapper.selectOne(wrapper);
        if (userCoupon != null) {
            throw new RuntimeException("优惠券已经领取过了");
        }

        //领取
        userCoupon = new UserCoupon();
        userCoupon.setUser_id(Integer.parseInt(user_id));
        userCoupon.setCoupon_id(Integer.parseInt(couponId));
        userCoupon.setUsed(0);
        userCouponMapper.insert(userCoupon);
    }

    @Override
    public List<Coupon> getCoupons() {
        String user_id = BaseContext.getCurrentId();
        //拿到所有可展示的优惠券
        List<Coupon> select = this.lambdaQuery().eq(Coupon::getStatus, "active").list();

        if (user_id != null) {
            //所有的优惠券看看对应的用户是否领取过
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getUser_id, user_id);
            List<UserCoupon> select1 = userCouponMapper.selectList(wrapper);

            //遍历所有的可领取的优惠券
            Iterator<Coupon> iterator = select.iterator();
            while (iterator.hasNext()){
                Coupon coupon = iterator.next();
                boolean found = false; // 标志变量
                //看看用户领取了没有
                for (UserCoupon userCoupon : select1) {
                    //领过了就让前端变灰
                    if (coupon.getId().equals(userCoupon.getCoupon_id())) {
                        coupon.setStatus("expired");
                        found = true; // 找到匹配的UserCoupon
                        break; // 既然找到了匹配的，就可以跳出内层循环
                    }
                }
                if (!found) {
                    // 如果没有找到匹配的UserCoupon，就移除当前的coupon
                    iterator.remove();
                }
            }
        }

        Set keys = redisTemplate.keys(YcConstants.GRAB_COUPON + "*");
        List<CouponRedis> list = redisTemplate.opsForValue().multiGet(keys);
        //拿到了所有的能抢的券了
        List<String> couponIds = new ArrayList<>();
        for (CouponRedis couponRedis : list) {
            couponIds.add(couponRedis.getCouponId());
        }
        List<Coupon> list1 = this.lambdaQuery().in(Coupon::getId, couponIds).list();
        for (Coupon coupon : list1) {
            coupon.setStatus("active");
            select.add(coupon);
        }

        return select;
    }

    @Override
    @Transactional
    public void useCoupon(String userId, Integer couponId) {
        LambdaUpdateWrapper<UserCoupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(UserCoupon::getUsed, 1)
                .eq(UserCoupon::getUser_id, userId)
                .eq(UserCoupon::getCoupon_id, couponId);
        userCouponMapper.update(null, updateWrapper);
    }

    /**
     * 抢券
     */
    @Override
    public void grab(Integer couponId) {
        CouponRedis o = (CouponRedis) redisTemplate.opsForValue().get(YcConstants.GRAB_COUPON + couponId);
        String userId = BaseContext.getCurrentId();

        //这个券没了就退出
        if (o == null) {
            return;
        }


        grabCoupon.grab(userId, couponId);
    }
}
