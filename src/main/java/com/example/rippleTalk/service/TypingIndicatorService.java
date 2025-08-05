package com.example.rippleTalk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TypingIndicatorService
{
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration TTL = Duration.ofSeconds(5);

    @Autowired
    public TypingIndicatorService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateTypingStatus(String conversationId, String userId, Boolean isTyping) {
        String key = buildRedisKey(conversationId, userId);

        if (isTyping) {
            redisTemplate.opsForValue().set(key, true, TTL);
        } else {
            redisTemplate.delete(key);
        }
    }

    public boolean isUserTyping(String conversationId, String userId) {
        String key = buildRedisKey(conversationId, userId);
        Object value = redisTemplate.opsForValue().get(key);
        if(value == null)
        {
            return false;
        }
        Boolean status = Boolean.valueOf(redisTemplate.opsForValue().get(key).toString());
        return Boolean.TRUE.equals(status);
    }

    private String buildRedisKey(String conversationId, String userId) {
        return String.format("typing:%s:%s", conversationId, userId);
    }
}
