package com.tutorapp.webapp.Chat.Presence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@ConditionalOnBean(PresenceService.class)
public class WebSocketPresenceListener {

    private final PresenceService presenceService;

    public WebSocketPresenceListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        presenceService.unregisterSession(event.getSessionId());
    }
}
