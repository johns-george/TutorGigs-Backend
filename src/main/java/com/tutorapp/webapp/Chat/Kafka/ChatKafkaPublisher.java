package com.tutorapp.webapp.Chat.Kafka;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;

@Service
@ConditionalOnProperty(name = "chat.kafka.enabled", havingValue = "true")
public class ChatKafkaPublisher {

    private final KafkaTemplate<String, Object> chatKafkaTemplate;

    public ChatKafkaPublisher(KafkaTemplate<String, Object> chatKafkaTemplate) {
        this.chatKafkaTemplate = chatKafkaTemplate;
    }

    public void publishCreated(ChatMessageDTO message, List<Long> recipientUserIds) {
        String key = message.getChatId() != null ? message.getChatId().toString() : null;
        chatKafkaTemplate.send(ChatKafkaTopics.MESSAGE_CREATED, key, new ChatDeliveryEvent(message, recipientUserIds));
    }

    public void publishToInstance(String instanceId, WsDeliverCommand command) {
        chatKafkaTemplate.send("chat.ws." + instanceId, instanceId, command);
    }
}
