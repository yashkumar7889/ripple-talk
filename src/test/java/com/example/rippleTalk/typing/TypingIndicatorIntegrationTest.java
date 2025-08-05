package com.example.rippleTalk.typing;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.dto.TypingIndicatorRequest;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.security.JwtUtils;
import com.example.rippleTalk.service.TypingIndicatorService;
import com.example.rippleTalk.util.TestUserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TypingIndicatorIntegrationTest
{

    @Autowired
    private TestRestTemplate restTemplate;

//    @Autowired
//    private RedisTemplate<Object, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TypingIndicatorService typingIndicatorService;

    private final List<String> createdTestUsernames = new ArrayList<>();

    private final String conversationId = "hoshoiaouuqepo1234";

    final String testEmail = TestUserUtil.generateUniqueTestEmail().toLowerCase();
    final String testUsername = TestUserUtil.generateUniqueUsername().toLowerCase();
    final String password = "yrehfhnkdfhfh";

    @BeforeEach
    void setup()
    {
        if(!createdTestUsernames.isEmpty())
        {
            userRepository.deleteByUsernameIn(createdTestUsernames);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(testEmail);
        user.setUsername(testUsername);
        user.setFullName("John Doe");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        createdTestUsernames.add(testUsername);

    }

    @AfterEach
    void cleanup()
    {
        if(!createdTestUsernames.isEmpty())
        {
            userRepository.deleteByUsernameIn(createdTestUsernames);
        }
    }

    @ParameterizedTest
    @ValueSource( booleans = {true, false})
    public void testTypingIndicatorUserTyping(boolean isUserTyping)
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );

        String token = response.getBody().getToken();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(token);

        // Constructing request for sending request for cache
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", conversationId);
        body.put("username", testUsername);
        body.put("isTyping", isUserTyping);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Perform POST request
        ResponseEntity<Void> cacheResponse = restTemplate.postForEntity("/api/typing", request, Void.class);
        assertEquals(typingIndicatorService.isUserTyping(conversationId, testUsername), isUserTyping, "User: " + testUsername + "must be typing");

        assertEquals(HttpStatus.OK, cacheResponse.getStatusCode());
    }

    @Test
    public void testUserStartsAndThenStopsTyping()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );

        String token = response.getBody().getToken();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(token);

        // Constructing request for sending request for cache
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", conversationId);
        body.put("username", testUsername);
        body.put("isTyping", true);

        HttpEntity<Map<String, Object>> request1 = new HttpEntity<>(body, headers);

        // Perform POST request when user starts typing
        ResponseEntity<Void> cacheResponse1 = restTemplate.postForEntity("/api/typing", request1, Void.class);
        assertTrue(typingIndicatorService.isUserTyping(conversationId, testUsername), "User: " + testUsername + "must be typing");
        assertEquals(HttpStatus.OK, cacheResponse1.getStatusCode());

        // Perform POST request when user stops typing
        body.put("isTyping", false);
        HttpEntity<Map<String, Object>> request2 = new HttpEntity<>(body, headers);

        // Perform POST request when user starts typing
        ResponseEntity<Void> cacheResponse2 = restTemplate.postForEntity("/api/typing", request2, Void.class);
        assertFalse(typingIndicatorService.isUserTyping(conversationId, testUsername), "User: " + testUsername + "must not be typing");
        assertEquals(HttpStatus.OK, cacheResponse2.getStatusCode());

    }

    private String getRedisKey(String conversationId, String userId)
    {
        return "typing:" + conversationId + ":" + userId;
    }
}
