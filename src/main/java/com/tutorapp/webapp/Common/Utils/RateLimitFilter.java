package com.tutorapp.webapp.Common.Utils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * In-memory sliding-window rate limiter.
 *
 * Each protected path has its own limit and window:
 *   POST /api/auth/login         → 10 requests / 15 min per IP
 *   POST /api/auth/signup        →  5 requests / 15 min per IP
 *   POST /api/forgot/**          →  5 requests / 15 min per IP
 *
 * Exceeding the limit returns 429 Too Many Requests.
 * Old entries are lazily evicted on access (no background thread).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // ── Per-path config ─────────────────────────────────────────
    private static final Map<String, PathConfig> PATH_CONFIGS = Map.of(
        "/api/auth/login",  new PathConfig(10, 15 * 60 * 1000L),
        "/api/auth/signup", new PathConfig(5,  15 * 60 * 1000L)
    );

    private static final String FORGOT_PREFIX = "/api/forgot/";
    private static final PathConfig FORGOT_CONFIG = new PathConfig(5, 15 * 60 * 1000L);

    // ── State ───────────────────────────────────────────────────
    // key = "IP:PATH"  →  sliding window counter
    private final ConcurrentHashMap<String, SlidingWindow> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        PathConfig config = resolveConfig(path);

        if (config == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String key = ip + ":" + path;
        long now = System.currentTimeMillis();

        SlidingWindow window = counters.computeIfAbsent(key, k -> new SlidingWindow());
        int count = window.incrementAndCount(now, config.windowMs);

        if (count > config.maxRequests) {
            long retryAfter = window.retryAfterMs(now, config.windowMs);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(Math.max(1, retryAfter / 1000)));
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}"
            );
            log.warn("Rate limit exceeded for {} on {} (count={}, limit={})",
                     ip, path, count, config.maxRequests);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private PathConfig resolveConfig(String path) {
        PathConfig exact = PATH_CONFIGS.get(path);
        if (exact != null) return exact;
        if (path.startsWith(FORGOT_PREFIX)) return FORGOT_CONFIG;
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        // Respect X-Forwarded-For when behind a reverse proxy
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // ── Inner helpers ───────────────────────────────────────────

    private static class PathConfig {
        final int maxRequests;
        final long windowMs;

        PathConfig(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
    }

    /**
     * Tracks timestamps of requests within a sliding window.
     * Old entries are evicted lazily on each access.
     */
    private static class SlidingWindow {
        private final ConcurrentHashMap<Long, Boolean> timestamps = new ConcurrentHashMap<>();

        /** Record a request at {@code now} and return the total count within the window. */
        int incrementAndCount(long now, long windowMs) {
            timestamps.put(now, Boolean.TRUE);
            evictExpired(now, windowMs);
            return timestamps.size();
        }

        /** Seconds until the oldest surviving request falls outside the window. */
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
