package com.tutorapp.webapp.Chat.Kafka;

import java.util.List;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;

public class ChatDeliveryEvent {
    private ChatMessageDTO message;
    private List<Long> recipientUserIds;

    public ChatDeliveryEvent() {
    }

    public ChatDeliveryEvent(ChatMessageDTO message, List<Long> recipientUserIds) {
        this.message = message;
        this.recipientUserIds = recipientUserIds;
    }

    public ChatMessageDTO getMessage() {
        return message;
    }

    public void setMessage(ChatMessageDTO message) {
        this.message = message;
    }

    public List<Long> getRecipientUserIds() {
        return recipientUserIds;
    }

    public void setRecipientUserIds(List<Long> recipientUserIds) {
        this.recipientUserIds = recipientUserIds;
    }
}
