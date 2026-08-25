package com.tutorapp.webapp.Chat.DTO;

import java.util.List;

import com.tutorapp.webapp.Chat.Enum.ChatType;


public class ChatDTO {
    private Long id;
    private ChatType type;  // "PRIVATE" or "GROUP"
    private String name;  // group name (null for private)
    private String timestamp;
    private String lastMessage; 
    private List<ParticipantDTO> participants;
    private List<ChatMessageDTO> messages;
  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage){
        this.lastMessage=lastMessage;
    }


    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDTO> participants) {
        this.participants = participants;
    }
}

