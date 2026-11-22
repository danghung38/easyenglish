package com.dxh.Elearning.aspect;

import com.dxh.Elearning.service.RateLimitService;
import com.dxh.Elearning.utils.IpUtil;
import com.dxh.Elearning.validator.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RateLimitService rateLimitService;
    private final HttpServletRequest request;

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(RateLimit rateLimit) {
        rateLimitService.checkLimit(
                rateLimit.action().name(),
                IpUtil.getClientIp(request),
                rateLimit.maxRequests(),
                Duration.ofMinutes(rateLimit.durationMinutes())
        );
    }
}
