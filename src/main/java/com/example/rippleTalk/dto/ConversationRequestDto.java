package com.example.rippleTalk.dto;

import com.example.rippleTalk.constants.RequestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ConversationRequestDto
{
    private String senderId;
    private String receiverId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING; // PENDING, ACCEPTED, REJECTED

    private Instant createdAt = Instant.now();
    private Instant updatedAt;
}
