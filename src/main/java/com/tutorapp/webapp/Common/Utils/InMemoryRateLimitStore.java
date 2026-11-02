package com.tutorapp.webapp.Common.Utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-instance fallback used when Redis is not configured.
 * Wired by {@code ServiceStoreConfig}, not by component scanning.
 */
public class InMemoryRateLimitStore implements RateLimitStore {

    private final ConcurrentHashMap<String, SlidingWindow> counters = new ConcurrentHashMap<>();

    @Override
    public int incrementAndCount(String key, long windowMs) {
        long now = System.currentTimeMillis();
        SlidingWindow window = counters.computeIfAbsent(key, k -> new SlidingWindow());
        return window.incrementAndCount(now, windowMs);
    }

    @Override
    public long retryAfterMs(String key, long windowMs) {
        SlidingWindow window = counters.get(key);
        if (window == null) {
            return windowMs;
        }
        return window.retryAfterMs(System.currentTimeMillis(), windowMs);
    }

    private static class SlidingWindow {
        private final ConcurrentHashMap<Long, Boolean> timestamps = new ConcurrentHashMap<>();

        int incrementAndCount(long now, long windowMs) {
            timestamps.put(now, Boolean.TRUE);
            evictExpired(now, windowMs);
            return timestamps.size();
        }

        long retryAfterMs(long now, long windowMs) {
            return timestamps.keySet().stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(now) + windowMs - now;
        }

        private void evictExpired(long now, long windowMs) {
            timestamps.keySet().removeIf(ts -> now - ts > windowMs);
        }
    }
}
