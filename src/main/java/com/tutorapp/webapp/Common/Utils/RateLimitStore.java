package com.tutorapp.webapp.Common.Utils;

public interface RateLimitStore {

    int incrementAndCount(String key, long windowMs);

    long retryAfterMs(String key, long windowMs);
}
