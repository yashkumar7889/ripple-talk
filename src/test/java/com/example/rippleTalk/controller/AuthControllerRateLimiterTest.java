package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.security.JwtUtils;
import com.example.rippleTalk.service.LoginRateLimiterService;
import com.example.rippleTalk.util.TestUserUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "security.ratelimit.user-attempts=5",
        "security.ratelimit.ip-attempts=10"
})
public class AuthControllerRateLimiterTest
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginRateLimiterService loginRateLimiterService;

    @Value("${security.ratelimit.user-attempts}")
    private int loginRateLimit;

    private final List<String> createdTestEmails = new ArrayList<>();

    final String testEmail = TestUserUtil.generateUniqueTestEmail().toLowerCase();
    final String testUsername = TestUserUtil.generateUniqueUsername().toLowerCase();
    final String password = "yrehfhnkdfhfh";

    @BeforeEach
    void setup()
    {
        loginRateLimiterService.clearIpLimitsForTests();
        if(!createdTestEmails.isEmpty())
        {
            userRepository.deleteByEmailIn(createdTestEmails);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(testEmail);
        user.setUsername(testUsername);
        user.setFullName("John Doe");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        createdTestEmails.add(testUsername);

    }

    @AfterEach
    void cleanup()
    {
        if(!createdTestEmails.isEmpty())
        {
            userRepository.deleteByEmailIn(createdTestEmails);
        }
    }

    @Test
    public void testLoginRateLimiting()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());

        for (int i = 0; i < loginRateLimit-1; i++)
        {
            response = restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode(), "Request " + (i+1) + " was rate limited prematurely");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().getToken());
        }

        ResponseEntity<String> rateLimitResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, rateLimitResponse.getStatusCode());
        assertTrue(rateLimitResponse.getBody().contains("Rate limit exceeded"));
    }

    @Test
    public void testUniqueIpLimit()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1st IP
        headers.set("X-Forwarded-For", "192.168.1.1");
        HttpEntity<LoginRequest> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<LoginResponse> response1 = restTemplate.postForEntity("/api/auth/login", entity, LoginResponse.class);
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody().getToken());

        // 2nd IP
        headers.set("X-Forwarded-For", "192.168.1.2");
        entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<LoginResponse> response2 = restTemplate.postForEntity("/api/auth/login", entity, LoginResponse.class);
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody().getToken());

        // 3rd IP — should be blocked
        headers.set("X-Forwarded-For", "192.168.1.3");
        entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<LoginResponse> response3 = restTemplate.postForEntity("/api/auth/login", entity, LoginResponse.class);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response3.getStatusCode());
    }

    private void createUser(String username)
    {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(TestUserUtil.generateUniqueTestEmail().toLowerCase());
        user.setUsername(username);
        user.setFullName("John Doe");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        createdTestEmails.add(username);
    }
}
