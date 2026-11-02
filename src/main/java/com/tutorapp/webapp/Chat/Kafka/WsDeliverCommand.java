package com.tutorapp.webapp.Chat.Kafka;

import java.util.List;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;

public class WsDeliverCommand {
    private ChatMessageDTO message;
    private List<Long> recipientUserIds;

    public WsDeliverCommand() {
    }

    public WsDeliverCommand(ChatMessageDTO message, List<Long> recipientUserIds) {
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
