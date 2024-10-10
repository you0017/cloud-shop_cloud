package com.yc.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.Item;
import com.yc.mapper.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class HotTask {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemMapper itemMapper;
    @Scheduled(cron = "0 0 0 * * ?")
    public void task() {
        //获取前六位销量的商品
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Item::getSold).eq(Item::getStatus,1)
                .last("limit 8");
        List<Item> items = itemMapper.selectList(wrapper);
        log.info("items:{}",items);
        //存进redis
        redisTemplate.opsForValue().set("hot",items);
    }
}
