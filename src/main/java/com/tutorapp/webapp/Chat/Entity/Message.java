package com.tutorapp.webapp.Chat.Entity;

import java.time.LocalDateTime;

import com.tutorapp.webapp.User.Entity.UserInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserInfo sender;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = true) // nullable = true
    private UserInfo receiver;
    private String content;
    private LocalDateTime timestamp;

     @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chats chat;

    // Constructors, getters, and setters

    public Long getId() {
        return id;
    }

    public UserInfo getSender() {
        return sender;
    }

    public void setSender(UserInfo sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Chats getChat() {
        return chat;
    }

    public void setChat(Chats chat) {
        this.chat = chat;
    }

    public UserInfo getReceiver() {
        return receiver;
    }

    public void setReceiver(UserInfo receiver) {
        this.receiver = receiver;
    }
    
}
