package com.dxh.Elearning.validator;

import com.dxh.Elearning.enums.RateLimitEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    RateLimitEnum action();
    int maxRequests();
    int durationMinutes();
}
