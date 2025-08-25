package com.example.rippleTalk.dto;

import com.example.rippleTalk.constants.RequestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ConversationRequestDto
{
    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Receiver ID is required")
    private String receiverId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING; // PENDING, ACCEPTED, REJECTED

    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    @AssertTrue(message = "Sender and Receiver cannot be the same")
    public boolean isSenderNotSameAsReceiver() {
        return senderId != null && receiverId != null && !senderId.equals(receiverId);
    }
}
