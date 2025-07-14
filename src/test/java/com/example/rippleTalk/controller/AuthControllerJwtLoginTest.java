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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
