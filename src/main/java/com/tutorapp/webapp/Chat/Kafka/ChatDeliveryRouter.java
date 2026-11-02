package com.tutorapp.webapp.Chat.Kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.tutorapp.webapp.Chat.Presence.PresenceService;
import com.tutorapp.webapp.Chat.Push.PushNotificationService;

@Component
@ConditionalOnProperty(name = "chat.kafka.enabled", havingValue = "true")
@ConditionalOnBean(PresenceService.class)
public class ChatDeliveryRouter {

    private static final Logger log = LoggerFactory.getLogger(ChatDeliveryRouter.class);

    private final PresenceService presenceService;
    private final ChatKafkaPublisher publisher;
    private final PushNotificationService pushNotificationService;

    public ChatDeliveryRouter(
            PresenceService presenceService,
            ChatKafkaPublisher publisher,
            PushNotificationService pushNotificationService) {
        this.presenceService = presenceService;
        this.publisher = publisher;
        this.pushNotificationService = pushNotificationService;
    }

    @KafkaListener(
            topics = ChatKafkaTopics.MESSAGE_CREATED,
            groupId = "chat-delivery-router",
            containerFactory = "chatKafkaListenerContainerFactory")
    public void route(ChatDeliveryEvent event) {
        if (event == null || event.getMessage() == null) {
            return;
        }
        List<Long> recipients = event.getRecipientUserIds();
        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients for chat message {}", event.getMessage().getId());
            return;
        }

        Map<String, List<Long>> onlineByInstance = new HashMap<>();
        for (Long recipientId : recipients) {
            presenceService.find(recipientId).ifPresentOrElse(
                    presence -> onlineByInstance
                            .computeIfAbsent(presence.getInstanceId(), key -> new ArrayList<>())
                            .add(recipientId),
                    () -> pushNotificationService.notifyOfflineUser(recipientId, event.getMessage()));
        }

        onlineByInstance.forEach((instanceId, userIds) ->
                publisher.publishToInstance(instanceId, new WsDeliverCommand(event.getMessage(), userIds)));
    }
}
