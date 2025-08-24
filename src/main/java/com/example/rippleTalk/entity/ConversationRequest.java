package com.example.rippleTalk.entity;

import com.example.rippleTalk.constants.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "conversation_requests")
@Getter
@Setter
public class ConversationRequest {
    @Id
    private String requestId;

    private String senderId;
    private String receiverId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING; // PENDING, ACCEPTED, REJECTED

    private Instant createdAt = Instant.now();
    private Instant updatedAt;
}
