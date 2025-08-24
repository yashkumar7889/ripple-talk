package com.example.rippleTalk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
public class Conversation
{
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(nullable = false)
    private String type;

    private String name;

    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
