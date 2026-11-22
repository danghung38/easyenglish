package com.dxh.Elearning.repo.redis;

import com.dxh.Elearning.entity.RedisToken;
import org.springframework.data.repository.CrudRepository;

public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
}
