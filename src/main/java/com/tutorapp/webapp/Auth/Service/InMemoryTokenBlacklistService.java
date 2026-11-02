package com.tutorapp.webapp.Auth.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-instance fallback used when Redis is not configured.
 * Wired by {@code ServiceStoreConfig}, not by component scanning.
 */
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Override
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
