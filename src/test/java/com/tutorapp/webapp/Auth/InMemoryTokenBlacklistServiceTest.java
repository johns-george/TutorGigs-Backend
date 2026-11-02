package com.tutorapp.webapp.Auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tutorapp.webapp.Auth.Service.InMemoryTokenBlacklistService;

class InMemoryTokenBlacklistServiceTest {

    private final InMemoryTokenBlacklistService service = new InMemoryTokenBlacklistService();

    @Test
    void blacklistedTokenIsRejected() {
        service.blacklistToken("jwt-a");
        assertTrue(service.isBlacklisted("jwt-a"));
    }

    @Test
    void nonBlacklistedTokenPasses() {
        service.blacklistToken("jwt-a");
        assertFalse(service.isBlacklisted("jwt-b"));
    }
}
