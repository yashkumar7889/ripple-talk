package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.TypingIndicatorRequest;
import com.example.rippleTalk.exception.BadRequestException;
import com.example.rippleTalk.service.TypingIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/typing")
public class TypingIndicatorController
{
    private final TypingIndicatorService typingService;

    @Autowired
    public TypingIndicatorController(TypingIndicatorService typingService) {
        this.typingService = typingService;
    }

    @PostMapping
    public ResponseEntity<?> updateTypingStatus(@RequestBody TypingIndicatorRequest request)
    {
        String username = request.getUsername();
        String conversationId = request.getConversationId();

        if(username == null || username.isBlank() || conversationId == null || conversationId.isBlank())
        {
            throw new BadRequestException("UserId or ConversationId cannot be null or blank");
        }

        typingService.updateTypingStatus(conversationId, username, request.getIsTyping());
        return ResponseEntity.ok().build();
    }
}
