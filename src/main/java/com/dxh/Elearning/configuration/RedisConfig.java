package com.dxh.Elearning.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.dxh.Elearning.repo.redis")
public class RedisConfig {
}
