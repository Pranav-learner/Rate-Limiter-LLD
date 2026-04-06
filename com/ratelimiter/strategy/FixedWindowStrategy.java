package com.ratelimiter.strategy;

import java.util.HashMap;
import java.util.Map;
import com.ratelimiter.model.RateLimiterConfig;

public class FixedWindowStrategy implements RateLimitStrategy {
    private RateLimiterConfig config;
    private Map<String, Integer> requestCounts;
    private Map<String, Long> windowStartTimes;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int INITIAL_COUNT = 0;
    private static final int INCREMENT_AMOUNT = 1;

    public FixedWindowStrategy(RateLimiterConfig config) {
        this.config = config;
        this.requestCounts = new HashMap<>();
        this.windowStartTimes = new HashMap<>();
    }

    @Override
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();

        if (!windowStartTimes.containsKey(clientId)) {
            windowStartTimes.put(clientId, now);
            requestCounts.put(clientId, INITIAL_COUNT);
        }

        long elapsed = now - windowStartTimes.get(clientId);
        long windowMillis = (long) config.getWindowSizeSeconds() * MILLISECONDS_PER_SECOND;

        if (elapsed >= windowMillis) {
            windowStartTimes.put(clientId, now);
            requestCounts.put(clientId, INITIAL_COUNT);
        }

        int currentCount = requestCounts.get(clientId);
        if (currentCount >= config.getMaxRequests()) {
            return false;
        }

        requestCounts.put(clientId, currentCount + INCREMENT_AMOUNT);
        return true;
    }

    @Override
    public RateLimiterConfig getConfig() {
        return config;
    }
}
