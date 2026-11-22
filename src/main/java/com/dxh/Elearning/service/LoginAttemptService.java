package com.dxh.Elearning.service;

import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_WINDOW = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public void assertNotBlocked(String username, String ip) {
        if (isBlocked("user", username) || isBlocked("ip", ip)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    public void loginFailed(String username, String ip) {
        increment("user", username);
        increment("ip", ip);
    }

    public void loginSucceeded(String username, String ip) {
        redis.delete(key("user", username));
        redis.delete(key("ip", ip));
    }

    private boolean isBlocked(String type, String value) {
        String count = redis.opsForValue().get(key(type, value));
        return count != null && Integer.parseInt(count) >= MAX_ATTEMPTS;
    }

    private void increment(String type, String value) {
        String key = key(type, value);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, BLOCK_WINDOW);
        }
    }

    private String key(String type, String value) {
        return "login:fail:" + type + ":" + value;
    }
}
