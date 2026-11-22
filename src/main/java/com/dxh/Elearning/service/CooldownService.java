package com.dxh.Elearning.service;

import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CooldownService {
    private final StringRedisTemplate redis;

    public void checkCooldown(String action, String key, long seconds) {
        String redisKey = "cd:" + action + ":" + key;
        Boolean exists = redis.hasKey(redisKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
        redis.opsForValue().set(redisKey, "1", Duration.ofSeconds(seconds));
    }
}
