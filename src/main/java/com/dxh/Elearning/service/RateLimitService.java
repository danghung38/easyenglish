package com.dxh.Elearning.service;

import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate redis;

    public void checkLimit(String action, String key, int maxRequests, Duration window) {
        String redisKey = "rl:" + action + ":" + key;
        Long count = redis.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redis.expire(redisKey, window);
        }
        if (count != null && count > maxRequests) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }
}
