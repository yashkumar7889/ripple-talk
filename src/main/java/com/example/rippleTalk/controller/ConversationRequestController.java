package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.ConversationRequestDto;
import com.example.rippleTalk.dto.ConversationRequestResponseDto;
import com.example.rippleTalk.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/conversation")
@RestController
@RequiredArgsConstructor
public class ConversationRequestController
{
    private final ConversationService conversationService;

    @PostMapping("/request")
    public ResponseEntity<ConversationRequestResponseDto> sendRequest(
            @RequestBody ConversationRequestDto dto) {
        return ResponseEntity.ok(conversationService.sendRequest(dto));
    }
}
