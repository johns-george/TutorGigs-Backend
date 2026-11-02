package com.tutorapp.webapp.Chat.Presence;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Common.Config.RedisConfig;

@Service
@ConditionalOnBean(RedisConfig.class)
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);
    static final String PRESENCE_KEY_PREFIX = "chat:presence:";
    static final String SESSION_KEY_PREFIX = "chat:session:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final WsInstanceIdentity instanceIdentity;
    private final long ttlSeconds;
    private final Map<String, Long> localSessions = new ConcurrentHashMap<>();

    public PresenceService(
            RedisTemplate<String, Object> redisTemplate,
            WsInstanceIdentity instanceIdentity,
            @Value("${chat.presence.ttl-seconds:90}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.instanceIdentity = instanceIdentity;
        this.ttlSeconds = ttlSeconds;
    }

    public void register(Long userId, String username, String sessionId) {
        PresenceRecord record = new PresenceRecord(
                userId, username, instanceIdentity.getInstanceId(), sessionId);
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        redisTemplate.opsForValue().set(presenceKey(userId), record, ttl);
        redisTemplate.opsForValue().set(sessionKey(sessionId), userId, ttl);
        localSessions.put(sessionId, userId);
        log.debug("Registered presence user={} instance={} session={}", userId, record.getInstanceId(), sessionId);
    }

    public void refresh(Long userId) {
        String key = presenceKey(userId);
        Object value = redisTemplate.opsForValue().get(key);
        if (!(value instanceof PresenceRecord record)) {
            return;
        }
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        redisTemplate.expire(key, ttl);
        if (record.getSessionId() != null) {
            redisTemplate.expire(sessionKey(record.getSessionId()), ttl);
        }
    }

    public void unregisterSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        localSessions.remove(sessionId);
        Object userIdVal = redisTemplate.opsForValue().get(sessionKey(sessionId));
        redisTemplate.delete(sessionKey(sessionId));
        Long userId = toLong(userIdVal);
        if (userId == null) {
            return;
        }
        Object value = redisTemplate.opsForValue().get(presenceKey(userId));
        if (value instanceof PresenceRecord record && sessionId.equals(record.getSessionId())) {
            redisTemplate.delete(presenceKey(userId));
        }
    }

    public Optional<PresenceRecord> find(Long userId) {
        Object value = redisTemplate.opsForValue().get(presenceKey(userId));
        if (value instanceof PresenceRecord record) {
            return Optional.of(record);
        }
        if (value instanceof java.util.Map<?, ?> map) {
            PresenceRecord record = new PresenceRecord();
            Object uid = map.get("userId");
            record.setUserId(toLong(uid));
            Object username = map.get("username");
            record.setUsername(username != null ? username.toString() : null);
            Object instanceId = map.get("instanceId");
            record.setInstanceId(instanceId != null ? instanceId.toString() : null);
            Object sessionId = map.get("sessionId");
            record.setSessionId(sessionId != null ? sessionId.toString() : null);
            return Optional.of(record);
        }
        return Optional.empty();
    }

    public boolean isLocal(PresenceRecord record) {
        return record != null && instanceIdentity.getInstanceId().equals(record.getInstanceId());
    }

    public Map<String, Long> getLocalSessions() {
        return localSessions;
    }

    private static String presenceKey(Long userId) {
        return PRESENCE_KEY_PREFIX + userId;
    }

    private static String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
