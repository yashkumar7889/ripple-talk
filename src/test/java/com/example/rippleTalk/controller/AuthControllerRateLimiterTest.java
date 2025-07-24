package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.security.JwtUtils;
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

    @Value("${security.ratelimit.user-attempts}")
    private int loginRateLimit;

    private final List<String> createdTestEmails = new ArrayList<>();

    final String testEmail = TestUserUtil.generateUniqueTestEmail();
    final String testUsername = TestUserUtil.generateUniqueUsername();
    final String password = "yrehfhnkdfhfh";

    @BeforeEach
    void setup()
    {
        if(!createdTestEmails.isEmpty())
        {
            userRepository.deleteByEmailIn(createdTestEmails);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(testEmail.toLowerCase());
        user.setUsername(testUsername.toLowerCase());
        user.setFullName("John Doe");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        createdTestEmails.add(testEmail);

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
    public void testLoginRateLimiting() {

        // Create the login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );
        // Send requests up to the limit
        for (int i = 0; i < loginRateLimit-1; i++)
        {
            response = restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            // You can assert 200 OK or whatever your actual logic returns for valid/invalid login
            assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode(), "Request " + (i+1) + " was rate limited prematurely");
        }

        ResponseEntity<String> rateLimitResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, rateLimitResponse.getStatusCode());
        assertTrue(rateLimitResponse.getBody().contains("Rate limit exceeded"));
    }
}
