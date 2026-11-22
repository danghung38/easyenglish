package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.VerifyType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@RedisHash("verification_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisVerificationToken implements Serializable {
    @Id
    String secretKey;

    @Indexed
    Long userId;

    @Indexed
    VerifyType verifyType;

    @TimeToLive
    Long ttl;
}
