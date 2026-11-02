package com.tutorapp.webapp.Chat.Presence;

public class PresenceRecord {
    private Long userId;
    private String username;
    private String instanceId;
    private String sessionId;

    public PresenceRecord() {
    }

    public PresenceRecord(Long userId, String username, String instanceId, String sessionId) {
        this.userId = userId;
        this.username = username;
        this.instanceId = instanceId;
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
