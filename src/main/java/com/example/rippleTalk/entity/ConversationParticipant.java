package com.example.rippleTalk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "conversation_participants")
@Getter
@Setter
public class ConversationParticipant {
    @Id
    private String id;

    private String conversationId;
    private String userId;
    private Instant joinedAt = Instant.now();
    private Boolean isAdmin;
}
