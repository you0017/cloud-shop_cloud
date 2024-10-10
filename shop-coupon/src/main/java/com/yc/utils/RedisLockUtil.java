package com.yc.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLockUtil {


    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String lockKey, String requestId, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, timeout, TimeUnit.SECONDS);
    }

    public void unlock(String lockKey, String requestId) {
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (currentValue != null && currentValue.equals(requestId)) {
            redisTemplate.delete(lockKey);
        }
    }
}
