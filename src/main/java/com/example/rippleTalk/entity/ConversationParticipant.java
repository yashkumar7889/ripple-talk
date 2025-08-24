package com.example.rippleTalk.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversation_participants")
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String conversationId;
    private String userId;
    private Instant joinedAt = Instant.now();
    private Boolean isAdmin;
}
