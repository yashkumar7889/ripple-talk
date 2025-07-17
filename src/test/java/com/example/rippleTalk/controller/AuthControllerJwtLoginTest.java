package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.security.JwtUtils;
import com.example.rippleTalk.util.TestUserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerJwtLoginTest
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        user.setEmail(testEmail);
        user.setUsername(testUsername);
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
    void test_JWTAuthentication()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        String token = jwtUtils.generateJwtToken(testUsername);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testLoginWithInvalidCredentials() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("nonExistingUser");
        loginRequest.setPassword("wrongPassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
    }

    @Test
    void testLoginWithEmptyPayload() throws Exception
    {
        // Create empty JSON payload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers); // Empty JSON object

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                entity,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseBodyMap = objectMapper.readValue(response.getBody(), Map.class);

        // Assert 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username and Password must not be null or blank", responseBodyMap.get("error"));
    }

    @Test
    void testAlreadyLoggedIn_TokenReuse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(testUsername);
        loginRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, headers);

        // First login
        ResponseEntity<LoginResponse> firstResponse = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                requestEntity,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertNotNull(firstResponse.getBody());
        String token = firstResponse.getBody().getToken();

        // Second login (simulate reuse scenario)
        ResponseEntity<LoginResponse> secondResponse = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                requestEntity,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());
        assertEquals(firstResponse.getBody().getToken(), secondResponse.getBody().getToken());
    }
}
