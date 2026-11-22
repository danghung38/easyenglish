package com.dxh.Elearning.service;

import com.dxh.Elearning.entity.RedisToken;
import com.dxh.Elearning.repo.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTokenRepository redisTokenRepository;

    public void blacklist(String tokenId, Date expiryTime) {
        if (tokenId == null || tokenId.isBlank() || expiryTime == null) {
            return;
        }

        long ttl = Duration.between(java.time.Instant.now(), expiryTime.toInstant()).getSeconds();
        if (ttl > 0) {
            redisTokenRepository.save(RedisToken.builder()
                    .id(tokenId)
                    .ttl(ttl)
                    .build());
        }
    }

    public boolean isBlacklisted(String tokenId) {
        return tokenId == null || tokenId.isBlank() || redisTokenRepository.existsById(tokenId);
    }
}
