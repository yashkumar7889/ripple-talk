package com.example.rippleTalk.service;

import io.github.bucket4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
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

    @Value("${security.ratelimit.max-unique-ips}")
    private int maxUniqueIps;

    @Value("${security.ratelimit.unique-ip-window-seconds}")
    private int uniqueIpWindowSeconds;

    @Value("${security.ratelimit.user-refill-interval-seconds}")
    private int userRefillIntervalSeconds;

    @Value("${security.ratelimit.ip-refill-interval-seconds}")
    private int ipRefillIntervalSeconds;

    private final Map<String, Instant> ipAccessTimes = new ConcurrentHashMap<>();

    // Maps to store buckets for each user and IP address
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public boolean tryConsumeUser(String username) {
        Bucket userBucket = userBuckets.computeIfAbsent(username, key ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(
                                maxUserLoginAttemptsPerMinute,
                                Refill.intervally(maxUserLoginAttemptsPerMinute, Duration.ofSeconds(userRefillIntervalSeconds))
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
                                Refill.greedy(maxIpLoginAttemptsPerMinute, Duration.ofSeconds(ipRefillIntervalSeconds))
                        ))
                        .build()
        );
        return ipBucket.tryConsume(1);
    }

    public boolean isIpLimitExceeded(String ip) {
        Instant now = Instant.now();

        // Clean up old IPs
        ipAccessTimes.entrySet().removeIf(entry ->
                Duration.between(entry.getValue(), now).getSeconds() > uniqueIpWindowSeconds);

        // Check if already allowed
        if (ipAccessTimes.containsKey(ip)) {
            return false;
        }

        // If adding this IP exceeds the limit
        if (ipAccessTimes.size() >= maxUniqueIps) {
            return true;
        }

        // Add the new IP to the map
        ipAccessTimes.put(ip, now);
        return false;
    }

    public void clearIpLimitsForTests() {
        ipBuckets.clear();
        ipAccessTimes.clear();
    }
}
