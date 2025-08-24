package com.example.rippleTalk.service;

import com.example.rippleTalk.constants.RequestStatus;
import com.example.rippleTalk.dto.ConversationRequestDto;
import com.example.rippleTalk.dto.ConversationRequestResponseDto;
import com.example.rippleTalk.entity.ConversationRequest;
import com.example.rippleTalk.repository.ConversationParticipantRepository;
import com.example.rippleTalk.repository.ConversationRepository;
import com.example.rippleTalk.repository.ConversationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService
{
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationRequestRepository requestRepository;

    // Step 1: Create request
    public ConversationRequestResponseDto sendRequest(ConversationRequestDto dto) {
        ConversationRequest request = new ConversationRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSenderId(dto.getSenderId());
        request.setReceiverId(dto.getReceiverId());
        request.setStatus(RequestStatus.PENDING);

        request = requestRepository.save(request);

        ConversationRequestResponseDto response = new ConversationRequestResponseDto();
        response.setRequestId(request.getRequestId());
        response.setSenderId(request.getSenderId());
        response.setReceiverId(request.getReceiverId());
        response.setStatus(request.getStatus().name());

        return response;
    }
}
