package com.example.rippleTalk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypingIndicatorRequest
{
    private String conversationId;
    private String username;
    private Boolean isTyping;
}
