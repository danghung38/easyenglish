package com.dxh.Elearning.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRateLimiter {
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final int requestLimit;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> requestsByIdentity = new ConcurrentHashMap<>();

    @Autowired
    public ChatRateLimiter(
            @Value("${chatbot.guardrails.requests-per-minute:20}") int requestLimit
    ) {
        this(requestLimit, WINDOW, Clock.systemUTC());
    }

    ChatRateLimiter(int requestLimit, Duration window, Clock clock) {
        if (requestLimit < 1) {
            throw new IllegalArgumentException("requestLimit must be positive");
        }
        this.requestLimit = requestLimit;
        this.window = window;
        this.clock = clock;
    }

    public boolean tryAcquire(String identity) {
        String safeIdentity = identity == null || identity.isBlank() ? "anonymous" : identity;
        Deque<Instant> requests = requestsByIdentity.computeIfAbsent(
                safeIdentity,
                ignored -> new ArrayDeque<>()
        );
        Instant now = clock.instant();
        Instant cutoff = now.minus(window);

        synchronized (requests) {
            while (!requests.isEmpty() && !requests.peekFirst().isAfter(cutoff)) {
                requests.removeFirst();
            }
            if (requests.size() >= requestLimit) {
                return false;
            }
            requests.addLast(now);
            return true;
        }
    }
}
