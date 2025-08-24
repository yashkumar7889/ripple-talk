package com.example.rippleTalk.service;

import com.example.rippleTalk.dto.*;
import com.example.rippleTalk.entity.Conversation;
import com.example.rippleTalk.entity.Message;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.ConversationRepository;
import com.example.rippleTalk.repository.MessageRepository;
import com.example.rippleTalk.repository.UserRepository;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ChatService
{
    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final UserRepository userRepo;

    public ChatService(MessageRepository messageRepo,
                       ConversationRepository conversationRepo,
                       UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageRequest req) {
        Conversation conv = conversationRepo.findById(req.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        User sender = userRepo.findByUsernameOrEmailIgnoreCase(req.getSenderId().toString(), req.getSenderId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        Message msg = new Message();
        msg.setId(UUID.randomUUID().toString());
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setContent(req.getContent());
        msg.setCreatedAt(Instant.now());
        msg.setMessageType(req.getMessageType());
        msg.setAttachmentUrl(req.getAttachmentUrl());
        msg.setReplyToMessageId(req.getReplyToMessageId());

        Message saved = messageRepo.save(msg);

        ChatMessageResponse res = new ChatMessageResponse();
        res.setMessageId(saved.getId());
        res.setConversationId(conv.getId());
        res.setSenderId(sender.getId());
        res.setSenderUsername(sender.getUsername());
        res.setContent(saved.getContent());
        res.setMessageType(saved.getMessageType());
        res.setAttachmentUrl(saved.getAttachmentUrl());
        res.setReplyToMessageId(saved.getReplyToMessageId());
        res.setCreatedAt(saved.getCreatedAt());
        return res;

    }
}
