package com.example.rippleTalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptConversationRequest
{
    @NotBlank(message = "Request ID is required")
    @NotNull(message = "Request ID cannot be null")
    private String requestId;

    @NotNull(message = "isAccepted flag is required")
    private Boolean isAccepted;
}
