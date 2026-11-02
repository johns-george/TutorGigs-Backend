package com.tutorapp.webapp.Chat.Presence;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class WsInstanceIdentity {

    private final String instanceId = UUID.randomUUID().toString();

    public String getInstanceId() {
        return instanceId;
    }

    public String getDeliverTopic() {
        return "chat.ws." + instanceId;
    }

    public String getConsumerGroup() {
        return "chat-ws-" + instanceId;
    }
}
