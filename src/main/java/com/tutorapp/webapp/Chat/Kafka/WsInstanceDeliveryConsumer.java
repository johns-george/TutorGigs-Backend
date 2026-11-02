package com.tutorapp.webapp.Chat.Kafka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.tutorapp.webapp.Chat.Presence.PresenceRecord;
import com.tutorapp.webapp.Chat.Presence.PresenceService;
import com.tutorapp.webapp.Chat.Presence.WsInstanceIdentity;
import com.tutorapp.webapp.Chat.Push.PushNotificationService;

@Component
@ConditionalOnProperty(name = "chat.kafka.enabled", havingValue = "true")
@ConditionalOnBean(PresenceService.class)
public class WsInstanceDeliveryConsumer {

    private static final Logger log = LoggerFactory.getLogger(WsInstanceDeliveryConsumer.class);

    private final PresenceService presenceService;
    private final WsInstanceIdentity instanceIdentity;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatKafkaPublisher publisher;
    private final PushNotificationService pushNotificationService;

    public WsInstanceDeliveryConsumer(
            PresenceService presenceService,
            WsInstanceIdentity instanceIdentity,
            SimpMessagingTemplate messagingTemplate,
            ChatKafkaPublisher publisher,
            PushNotificationService pushNotificationService) {
        this.presenceService = presenceService;
        this.instanceIdentity = instanceIdentity;
        this.messagingTemplate = messagingTemplate;
        this.publisher = publisher;
        this.pushNotificationService = pushNotificationService;
    }

    @KafkaListener(
            topics = "#{wsInstanceIdentity.deliverTopic}",
            groupId = "#{wsInstanceIdentity.consumerGroup}",
            containerFactory = "chatKafkaListenerContainerFactory")
    public void deliver(WsDeliverCommand command) {
        if (command == null || command.getMessage() == null) {
            return;
        }
        List<Long> recipientIds = command.getRecipientUserIds();
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }

        boolean sentToTopic = false;
        for (Long recipientId : recipientIds) {
            PresenceRecord presence = presenceService.find(recipientId).orElse(null);
            if (presence == null) {
                pushNotificationService.notifyOfflineUser(recipientId, command.getMessage());
                continue;
            }
            if (!instanceIdentity.getInstanceId().equals(presence.getInstanceId())) {
                log.debug("Recipient {} moved to instance {}, rerouting", recipientId, presence.getInstanceId());
                publisher.publishToInstance(
                        presence.getInstanceId(),
                        new WsDeliverCommand(command.getMessage(), List.of(recipientId)));
                continue;
            }
            try {
                if (!sentToTopic) {
                    messagingTemplate.convertAndSend(
                            "/topic/chat/" + command.getMessage().getChatId(),
                            command.getMessage());
                    sentToTopic = true;
                }
                if (presence.getUsername() != null) {
                    messagingTemplate.convertAndSendToUser(
                            presence.getUsername(), "/queue/messages", command.getMessage());
                }
            } catch (RuntimeException ex) {
                log.warn("WebSocket deliver failed for user {}, will retry", recipientId, ex);
                throw ex;
            }
        }
    }
}
