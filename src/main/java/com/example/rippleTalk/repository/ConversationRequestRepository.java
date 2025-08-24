package com.example.rippleTalk.repository;

import com.example.rippleTalk.constants.RequestStatus;
import com.example.rippleTalk.entity.ConversationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRequestRepository extends JpaRepository<ConversationRequest, String> {
    Optional<ConversationRequest> findBySenderIdAndReceiverIdAndStatus(String senderId, String receiverId, RequestStatus status);
}
