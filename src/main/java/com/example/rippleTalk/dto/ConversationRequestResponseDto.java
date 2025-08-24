package com.example.rippleTalk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationRequestResponseDto {
    private String requestId;
    private String senderId;
    private String receiverId;
    private String status;
}
