package com.dxh.Elearning.repo.redis;

import com.dxh.Elearning.entity.RedisVerificationToken;
import com.dxh.Elearning.enums.VerifyType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedisVerificationTokenRepository extends CrudRepository<RedisVerificationToken, String> {
    List<RedisVerificationToken> findByUserIdAndVerifyType(Long userId, VerifyType verifyType);
}
