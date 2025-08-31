package com.example.rippleTalk.conversation;

import com.example.rippleTalk.constants.RequestStatus;
import com.example.rippleTalk.dto.*;
import com.example.rippleTalk.entity.User;
import com.example.rippleTalk.repository.UserRepository;
import com.example.rippleTalk.security.JwtUtils;
import com.example.rippleTalk.util.TestUserUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ConversationRequestTest
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

    private final List<String> createdConversationIds = new ArrayList<>();

    private static String password = "hehsobsdhodhiobsdd";

    @AfterEach
    void cleanup()
    {
        if(!createdTestEmails.isEmpty())
        {
            userRepository.deleteByEmailIn(createdTestEmails);
        }
    }

    @Test
    public void testConversationRequestSentSuccessfully()
    {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUserName(username1);
        loginRequest1.setPassword(password);

        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUserName(username2);
        loginRequest2.setPassword(password);

        ResponseEntity<LoginResponse> response1 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest1,
                LoginResponse.class
        );

        ResponseEntity<LoginResponse> response2 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest2,
                LoginResponse.class
        );

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        System.out.println("Hel");

        ConversationRequestDto conversationRequestDto = new ConversationRequestDto();
        conversationRequestDto.setSenderId(username1);
        conversationRequestDto.setReceiverId(username2);
        conversationRequestDto.setStatus(RequestStatus.PENDING);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = restTemplate.getRootUri() + "/api/conversation/request";

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = restTemplate.postForEntity(
                url,
                requestEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());
    }

    @Test
    public void testConversationRequestAcceptedSuccessfully()
    {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUserName(username1);
        loginRequest1.setPassword(password);

        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUserName(username2);
        loginRequest2.setPassword(password);

        ResponseEntity<LoginResponse> response1 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest1,
                LoginResponse.class
        );

        ResponseEntity<LoginResponse> response2 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest2,
                LoginResponse.class
        );

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        System.out.println("Hel");

        ConversationRequestDto conversationRequestDto = new ConversationRequestDto();
        conversationRequestDto.setSenderId(username1);
        conversationRequestDto.setReceiverId(username2);
        conversationRequestDto.setStatus(RequestStatus.PENDING);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = restTemplate.getRootUri() + "/api/conversation/request";

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = restTemplate.postForEntity(
                url,
                requestEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = new AcceptConversationRequest();
        acceptConversationRequest.setRequestId(conversationRequestResponse.getBody().getRequestId());
        acceptConversationRequest.setIsAccepted(true);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(acceptConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";

        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = restTemplate.postForEntity(
                acceptConversationRequestUrl,
                acceptConversationRequestHttpEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("Post accepting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post accepting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post accepting request status are not equal",RequestStatus.ACCEPTED.toString(), acceptRequestResponse.getBody().getStatus());
    }

    @Test
    public void testConversationRequestRejectedSuccessfully()
    {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUserName(username1);
        loginRequest1.setPassword(password);

        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUserName(username2);
        loginRequest2.setPassword(password);

        ResponseEntity<LoginResponse> response1 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest1,
                LoginResponse.class
        );

        ResponseEntity<LoginResponse> response2 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest2,
                LoginResponse.class
        );

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        System.out.println("Hel");

        ConversationRequestDto conversationRequestDto = new ConversationRequestDto();
        conversationRequestDto.setSenderId(username1);
        conversationRequestDto.setReceiverId(username2);
        conversationRequestDto.setStatus(RequestStatus.PENDING);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = restTemplate.getRootUri() + "/api/conversation/request";

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = restTemplate.postForEntity(
                url,
                requestEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = new AcceptConversationRequest();
        acceptConversationRequest.setRequestId(conversationRequestResponse.getBody().getRequestId());
        acceptConversationRequest.setIsAccepted(false);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(acceptConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";

        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = restTemplate.postForEntity(
                acceptConversationRequestUrl,
                acceptConversationRequestHttpEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("Post rejecting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post rejecting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post rejecting request status are not equal",RequestStatus.REJECTED.toString(), acceptRequestResponse.getBody().getStatus());
    }

    @Test
    public void testRejectAlreadyRejectedConversation() throws JsonProcessingException {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUserName(username1);
        loginRequest1.setPassword(password);

        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUserName(username2);
        loginRequest2.setPassword(password);

        ResponseEntity<LoginResponse> response1 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest1,
                LoginResponse.class
        );

        ResponseEntity<LoginResponse> response2 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest2,
                LoginResponse.class
        );

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        System.out.println("Hel");

        ConversationRequestDto conversationRequestDto = new ConversationRequestDto();
        conversationRequestDto.setSenderId(username1);
        conversationRequestDto.setReceiverId(username2);
        conversationRequestDto.setStatus(RequestStatus.PENDING);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = restTemplate.getRootUri() + "/api/conversation/request";

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = restTemplate.postForEntity(
                url,
                requestEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest rejectConversationRequest = new AcceptConversationRequest();
        rejectConversationRequest.setRequestId(conversationRequestResponse.getBody().getRequestId());
        rejectConversationRequest.setIsAccepted(false);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(rejectConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";

        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = restTemplate.postForEntity(
                acceptConversationRequestUrl,
                acceptConversationRequestHttpEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("Post rejecting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post rejecting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post rejecting request status are not equal",RequestStatus.REJECTED.toString(), acceptRequestResponse.getBody().getStatus());

        ResponseEntity<String> rejectRequestResponse2 = restTemplate.postForEntity(
                acceptConversationRequestUrl,
                acceptConversationRequestHttpEntity,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = mapper.readValue(rejectRequestResponse2.getBody(), ErrorResponse.class);
        Assert.assertEquals("Status codes are not equal.", HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        Assert.assertNotNull(errorResponse.getMessage());
    }

    @Test
    public void testAcceptAlreadyRejectedConversation() throws JsonProcessingException {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUserName(username1);
        loginRequest1.setPassword(password);

        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUserName(username2);
        loginRequest2.setPassword(password);

        ResponseEntity<LoginResponse> response1 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest1,
                LoginResponse.class
        );

        ResponseEntity<LoginResponse> response2 = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest2,
                LoginResponse.class
        );

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        System.out.println("Hel");

        ConversationRequestDto conversationRequestDto = new ConversationRequestDto();
        conversationRequestDto.setSenderId(username1);
        conversationRequestDto.setReceiverId(username2);
        conversationRequestDto.setStatus(RequestStatus.PENDING);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = restTemplate.getRootUri() + "/api/conversation/request";

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = restTemplate.postForEntity(
                url,
                requestEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest rejectConversationRequest = new AcceptConversationRequest();
        rejectConversationRequest.setRequestId(conversationRequestResponse.getBody().getRequestId());
        rejectConversationRequest.setIsAccepted(false);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(rejectConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";

        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = restTemplate.postForEntity(
                acceptConversationRequestUrl,
                acceptConversationRequestHttpEntity,
                ConversationRequestResponseDto.class);

        Assert.assertEquals("Post rejecting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post rejecting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post rejecting request status are not equal",RequestStatus.REJECTED.toString(), acceptRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = new AcceptConversationRequest();
        acceptConversationRequest.setRequestId(conversationRequestResponse.getBody().getRequestId());
        acceptConversationRequest.setIsAccepted(true);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity2 =
                new HttpEntity<>(acceptConversationRequest, headers);


        ResponseEntity<String> rejectRequestResponse2 = restTemplate.postForEntity(
                acceptConversationRequestUrl,
                acceptConversationRequestHttpEntity2,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = mapper.readValue(rejectRequestResponse2.getBody(), ErrorResponse.class);
        Assert.assertEquals("Status codes are not equal.", HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        Assert.assertNotNull(errorResponse.getMessage());
    }

    public  User createUser()
    {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(TestUserUtil.generateUniqueTestEmail());
        user.setUsername(TestUserUtil.generateUniqueUsername());
        user.setFullName("John Doe");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(User.Status.ACTIVE);
        return userRepository.save(user);
    }
}
