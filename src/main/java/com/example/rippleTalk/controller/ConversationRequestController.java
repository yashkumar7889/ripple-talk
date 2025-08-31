package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.AcceptConversationRequest;
import com.example.rippleTalk.dto.ConversationRequestDto;
import com.example.rippleTalk.dto.ConversationRequestResponseDto;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.exception.ResourceNotFoundException;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/api/conversation")
@RestController
@RequiredArgsConstructor
public class ConversationRequestController
{
    private final ConversationService conversationService;

    private final UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<ConversationRequestResponseDto> sendRequest(
           @Valid @RequestBody ConversationRequestDto dto) {
        List<String> missingUserIds = getMissingUsers(List.of(dto.getReceiverId(), dto.getSenderId()));
        if(!missingUserIds.isEmpty())
        {
            String errorMessage = "One or more users not found: " + String.join(", ", missingUserIds);
            throw new ResourceNotFoundException(errorMessage);
        }

        return ResponseEntity.ok(conversationService.sendRequest(dto));
    }

    @PostMapping("/request/respond")
    public ResponseEntity<ConversationRequestResponseDto> respondToRequest(
           @Valid @RequestBody AcceptConversationRequest dto) {

        return ResponseEntity.ok(conversationService.respondToRequest(dto));
    }

    private List<String> getMissingUsers(List<String> userIds)
    {
        List<User> users = userRepository.findByUsernameInOrEmailInIgnoreCase(userIds);
        Set<String> foundUserIds = users.stream().map(User::getUsername).collect(Collectors.toSet());
        return userIds.stream()
                .filter(id -> !foundUserIds.contains(id)).toList();
    }
}
