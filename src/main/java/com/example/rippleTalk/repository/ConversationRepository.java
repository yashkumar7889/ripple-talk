package com.example.rippleTalk.repository;

import com.example.rippleTalk.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String>
{
}
