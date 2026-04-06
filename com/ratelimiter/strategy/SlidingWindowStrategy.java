package com.ratelimiter.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ratelimiter.model.RateLimiterConfig;

public class SlidingWindowStrategy implements RateLimitStrategy {
    private RateLimiterConfig config;
    private Map<String, List<Long>> requestTimestamps;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    public SlidingWindowStrategy(RateLimiterConfig config) {
        this.config = config;
        this.requestTimestamps = new HashMap<>();
    }

    @Override
    public boolean isAllowed(String clientId) {
        if (!requestTimestamps.containsKey(clientId)) {
            requestTimestamps.put(clientId, new ArrayList<>());
        }

        long now = System.currentTimeMillis();
        List<Long> timestamps = requestTimestamps.get(clientId);
        List<Long> validTimestamps = new ArrayList<>();
        long windowMillis = (long) config.getWindowSizeSeconds() * MILLISECONDS_PER_SECOND;

        for (Long timestamp : timestamps) {
            long age = now - timestamp;
            if (age <= windowMillis) {
                validTimestamps.add(timestamp);
            }
        }

        requestTimestamps.put(clientId, validTimestamps);
        int currentCount = validTimestamps.size();

        if (currentCount >= config.getMaxRequests()) {
            return false;
        }

        validTimestamps.add(now);
        return true;
    }

    @Override
    public RateLimiterConfig getConfig() {
        return config;
    }
}
