package com.example.rippleTalk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptConversationRequest
{
    private String requestId;
    private boolean isAccepted;
}
