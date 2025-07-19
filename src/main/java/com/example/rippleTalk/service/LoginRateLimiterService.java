package com.example.rippleTalk.service;

import io.github.bucket4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService
{
    // These values will be injected from application.properties
    @Value("${security.ratelimit.user-attempts}")
    private int maxUserLoginAttemptsPerMinute;

    @Value("${security.ratelimit.ip-attempts}")
    private int maxIpLoginAttemptsPerMinute;

    // Maps to store buckets for each user and IP address
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public boolean tryConsumeUser(String username) {
        Bucket userBucket = userBuckets.computeIfAbsent(username, key ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(
                                maxUserLoginAttemptsPerMinute,
                                Refill.greedy(maxUserLoginAttemptsPerMinute, Duration.ofMinutes(1))
                        ))
                        .build()
        );
        return userBucket.tryConsume(1);
    }

    public boolean tryConsumeIp(String ipAddress) {
        Bucket ipBucket = ipBuckets.computeIfAbsent(ipAddress, key ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(
                                maxIpLoginAttemptsPerMinute,
                                Refill.greedy(maxIpLoginAttemptsPerMinute, Duration.ofMinutes(1))
                        ))
                        .build()
        );
        return ipBucket.tryConsume(1);
    }
}
