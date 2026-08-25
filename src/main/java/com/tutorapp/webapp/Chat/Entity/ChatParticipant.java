package com.tutorapp.webapp.Chat.Entity;

import java.time.LocalDateTime;

import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Enum.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chats chat;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;  
    private LocalDateTime joinedAt = LocalDateTime.now();
     @Enumerated(EnumType.STRING) 
    private Role role; // ADMIN / MEMBER

       protected ChatParticipant() {
        // Hibernate needs this
        }
        
       public ChatParticipant(Chats chat, UserInfo user, Role role) {
        this.chat = chat;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public Chats getChat() {
        return chat;
    }

    public void setChat(Chats chat) {
        this.chat = chat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}