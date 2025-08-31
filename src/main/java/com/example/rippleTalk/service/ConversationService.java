package com.example.rippleTalk.service;

import com.example.rippleTalk.constants.ConversationType;
import com.example.rippleTalk.constants.RequestStatus;
import com.example.rippleTalk.dto.AcceptConversationRequest;
import com.example.rippleTalk.dto.ConversationRequestDto;
import com.example.rippleTalk.dto.ConversationRequestResponseDto;
import com.example.rippleTalk.entity.Conversation;
import com.example.rippleTalk.entity.ConversationParticipant;
import com.example.rippleTalk.entity.ConversationRequest;
import com.example.rippleTalk.exception.BadRequestException;
import com.example.rippleTalk.exception.ConflictException;
import com.example.rippleTalk.exception.ResourceNotFoundException;
import com.example.rippleTalk.repository.ConversationParticipantRepository;
import com.example.rippleTalk.repository.ConversationRepository;
import com.example.rippleTalk.repository.ConversationRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService
{
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationRequestRepository requestRepository;

    public ConversationRequestResponseDto sendRequest(ConversationRequestDto dto)
    {
        if(dto.getSenderId().equals(dto.getReceiverId()))
        {
            throw new BadRequestException("sender and receiver ids cannot be the same");
        }
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

    @Transactional
    public ConversationRequestResponseDto respondToRequest(AcceptConversationRequest acceptConversationRequest)
    {

        ConversationRequest conversationRequest = requestRepository.findById(acceptConversationRequest.getRequestId()).orElseThrow(() -> new ResourceNotFoundException("Request does not exist"));

        if (conversationRequest.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("This request has already been " + conversationRequest.getStatus());
        }

        if(acceptConversationRequest.getIsAccepted())
        {
            conversationRequest.setStatus(RequestStatus.ACCEPTED);
            conversationRequest.setUpdatedAt(Instant.now());

            Conversation conversation = new Conversation();
            conversation.setId(UUID.randomUUID().toString());
            conversation.setCreatedBy(conversationRequest.getSenderId());
            conversation.setType(ConversationType.PRIVATE.toString());

            conversation = conversationRepository.save(conversation);

            Instant currentDatAndTime = Instant.now();

            ConversationParticipant conversationParticipant1 = new ConversationParticipant();
            conversationParticipant1.setId(UUID.randomUUID().toString());
            conversationParticipant1.setConversationId(conversation.getId());
            conversationParticipant1.setUserId(conversationRequest.getSenderId());
            conversationParticipant1.setJoinedAt(currentDatAndTime);

            ConversationParticipant conversationParticipant2 = new ConversationParticipant();
            conversationParticipant2.setId(UUID.randomUUID().toString());
            conversationParticipant2.setConversationId(conversation.getId());
            conversationParticipant2.setUserId(conversationRequest.getReceiverId());
            conversationParticipant2.setJoinedAt(currentDatAndTime);

            participantRepository.save(conversationParticipant1);
            participantRepository.save(conversationParticipant2);
            //participantRepository.saveAll(List.of(conversationParticipant1, conversationParticipant2));

        }
        else
        {
            conversationRequest.setStatus(RequestStatus.REJECTED);
            conversationRequest.setUpdatedAt(Instant.now());
        }

        requestRepository.save(conversationRequest);

        ConversationRequestResponseDto conversationRequestResponse = new ConversationRequestResponseDto();
        conversationRequestResponse.setRequestId(acceptConversationRequest.getRequestId());
        conversationRequestResponse.setReceiverId(conversationRequest.getReceiverId());
        conversationRequestResponse.setSenderId(conversationRequest.getSenderId());
        conversationRequestResponse.setStatus(conversationRequest.getStatus().name());

        return conversationRequestResponse;

    }
}
