package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.RegisterRequest;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.util.TestUserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final List<String> createdTestEmails = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (String email : createdTestEmails) {
            userRepository.deleteByEmail(email);
        }
        createdTestEmails.clear();
    }

    @Test
    void testRegisterEndpoint() throws Exception
    {
        String testEmail = TestUserUtil.generateUniqueTestEmail();
        String testUsername = TestUserUtil.generateUniqueUsername();

        RegisterRequest request = new RegisterRequest();
        request.setUserName(testUsername);
        request.setEmail(testEmail);
        request.setPassword("password123");
        request.setFullName("Jane Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(testUsername)))
                .andExpect(jsonPath("$.email", is(testEmail)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        createdTestEmails.add(testEmail);
    }

    @Test
    void register_throwsError_whenEmailAlreadyExists() throws Exception
    {
        String testEmail = TestUserUtil.generateUniqueTestEmail();
        String testUsername = TestUserUtil.generateUniqueUsername();

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(testEmail);
        user.setUsername(testUsername);
        user.setFullName("John Doe");
        user.setPasswordHash("encoded_password");
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        createdTestEmails.add(testEmail);


        RegisterRequest request = new RegisterRequest();
        request.setUserName(testUsername);
        request.setEmail(testEmail);
        request.setPassword("password123");
        request.setFullName("Jane Doe");


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already in use"));

    }
}
