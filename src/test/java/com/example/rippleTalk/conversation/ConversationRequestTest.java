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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        LoginRequest loginRequest1 = createLoginRequest(username1, password);
        ResponseEntity<LoginResponse> response1 = sendPostRequest(getLoginURL(), loginRequest1, LoginResponse.class);

        LoginRequest loginRequest2 = createLoginRequest(username2, password);
        ResponseEntity<LoginResponse> response2 = sendPostRequest(getLoginURL(), loginRequest2, LoginResponse.class);

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        ConversationRequestDto conversationRequestDto = createConversationRequest(username1, username2, RequestStatus.PENDING);
        HttpHeaders headers = createHttpHeaders(MediaType.APPLICATION_JSON, token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = sendPostRequest(getConversationRequestURL(), requestEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());
    }

    @Test
    public void testConversationRequestWithSameSenderIds() throws JsonProcessingException {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        LoginRequest loginRequest = createLoginRequest(username1, password);
        ResponseEntity<LoginResponse> response = sendPostRequest(getLoginURL(), loginRequest, LoginResponse.class);

        ConversationRequestDto conversationRequestDto = createConversationRequest(username1, username1, RequestStatus.PENDING);
        HttpHeaders headers = createHttpHeaders(MediaType.APPLICATION_JSON, response.getBody().getToken());

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        ResponseEntity<String> conversationRequestResponse = sendPostRequest(getConversationRequestURL(), requestEntity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseBodyMap = objectMapper.readValue(conversationRequestResponse.getBody(), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, conversationRequestResponse.getStatusCode());
        assertEquals("sender and receiver ids cannot be the same", responseBodyMap.get("error"));
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

        LoginRequest loginRequest1 = createLoginRequest(username1, password);
        ResponseEntity<LoginResponse> response1 = sendPostRequest(getLoginURL(), loginRequest1, LoginResponse.class);

        LoginRequest loginRequest2 = createLoginRequest(username2, password);
        ResponseEntity<LoginResponse> response2 = sendPostRequest(getLoginURL(), loginRequest2, LoginResponse.class);

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        ConversationRequestDto conversationRequestDto = createConversationRequest(username1, username2, RequestStatus.PENDING);
        HttpHeaders headers = createHttpHeaders(MediaType.APPLICATION_JSON, token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = "/api/conversation/request";
        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = sendPostRequest(url, requestEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = createAcceptConversationRequest(conversationRequestResponse.getBody().getRequestId(), true);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(acceptConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";
        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = sendPostRequest(acceptConversationRequestUrl, acceptConversationRequestHttpEntity, ConversationRequestResponseDto.class);

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

        LoginRequest loginRequest1 = createLoginRequest(username1, password);
        ResponseEntity<LoginResponse> response1 = sendPostRequest(getLoginURL(), loginRequest1, LoginResponse.class);

        LoginRequest loginRequest2 = createLoginRequest(username2, password);
        ResponseEntity<LoginResponse> response2 = sendPostRequest(getLoginURL(), loginRequest2, LoginResponse.class);

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        ConversationRequestDto conversationRequestDto = createConversationRequest(username1, username2, RequestStatus.PENDING);
        HttpHeaders headers = createHttpHeaders(MediaType.APPLICATION_JSON, token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = sendPostRequest(getConversationRequestURL(), requestEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = createAcceptConversationRequest(conversationRequestResponse.getBody().getRequestId(), false);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(acceptConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";
        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse =sendPostRequest(acceptConversationRequestUrl, acceptConversationRequestHttpEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("Post rejecting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post rejecting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post rejecting request status are not equal",RequestStatus.REJECTED.toString(), acceptRequestResponse.getBody().getStatus());
    }

    @ParameterizedTest
    @ValueSource( booleans = {true, false})
    public void testRejectedConversation(boolean isAccepted) throws JsonProcessingException {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = createLoginRequest(username1, password);
        ResponseEntity<LoginResponse> response1 = sendPostRequest(getLoginURL(), loginRequest1, LoginResponse.class);

        LoginRequest loginRequest2 = createLoginRequest(username2, password);
        ResponseEntity<LoginResponse> response2 = sendPostRequest(getLoginURL(), loginRequest2, LoginResponse.class);

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        ConversationRequestDto conversationRequestDto = createConversationRequest(username1, username2, RequestStatus.PENDING);
        HttpHeaders headers = createHttpHeaders(MediaType.APPLICATION_JSON, token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = sendPostRequest(getConversationRequestURL(), requestEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest rejectConversationRequest = createAcceptConversationRequest(conversationRequestResponse.getBody().getRequestId(), false);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(rejectConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";

        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = sendPostRequest(acceptConversationRequestUrl, acceptConversationRequestHttpEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("Post rejecting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post rejecting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post rejecting request status are not equal",RequestStatus.REJECTED.toString(), acceptRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = createAcceptConversationRequest(conversationRequestResponse.getBody().getRequestId(), isAccepted);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity2 =
                new HttpEntity<>(acceptConversationRequest, headers);
        ResponseEntity<String> rejectRequestResponse2 = sendPostRequest(acceptConversationRequestUrl, acceptConversationRequestHttpEntity2, String.class);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = mapper.readValue(rejectRequestResponse2.getBody(), ErrorResponse.class);
        Assert.assertEquals("Status codes are not equal.", HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        Assert.assertNotNull(errorResponse.getMessage());
    }

    @ParameterizedTest
    @ValueSource( booleans = {true, false})
    public void testAcceptedConversationRequest(boolean isAccepted) throws JsonProcessingException {
        final User user1 = createUser();
        final String username1 = user1.getUsername();
        createdTestEmails.add(username1);

        final User user2 = createUser();
        final String username2 = user2.getUsername();
        createdTestEmails.add(username2);

        LoginRequest loginRequest1 = createLoginRequest(username1, password);
        ResponseEntity<LoginResponse> response1 = sendPostRequest(getLoginURL(), loginRequest1, LoginResponse.class);

        LoginRequest loginRequest2 = createLoginRequest(username2, password);
        ResponseEntity<LoginResponse> response2 = sendPostRequest(getLoginURL(), loginRequest2, LoginResponse.class);

        String token1 = response1.getBody().getToken();
        String token2 = response2.getBody().getToken();

        ConversationRequestDto conversationRequestDto = createConversationRequest(username1, username2, RequestStatus.PENDING);
        HttpHeaders headers = createHttpHeaders(MediaType.APPLICATION_JSON, token1);

        HttpEntity<ConversationRequestDto> requestEntity =
                new HttpEntity<>(conversationRequestDto, headers);

        String url = "/api/conversation/request";
        ResponseEntity<ConversationRequestResponseDto> conversationRequestResponse = sendPostRequest(url, requestEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("senderIds are not equal", conversationRequestDto.getSenderId(), conversationRequestResponse.getBody().getSenderId());
        Assert.assertEquals("receiverIds are not equal", conversationRequestDto.getReceiverId(), conversationRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("conversation request status are not equal",conversationRequestDto.getStatus().toString(), conversationRequestResponse.getBody().getStatus());

        AcceptConversationRequest acceptConversationRequest = createAcceptConversationRequest(conversationRequestResponse.getBody().getRequestId(), true);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity =
                new HttpEntity<>(acceptConversationRequest, headers);

        String acceptConversationRequestUrl = restTemplate.getRootUri() + "/api/conversation/request/respond";
        ResponseEntity<ConversationRequestResponseDto> acceptRequestResponse = sendPostRequest(acceptConversationRequestUrl, acceptConversationRequestHttpEntity, ConversationRequestResponseDto.class);

        Assert.assertEquals("Post accepting request senderIds are not equal", conversationRequestDto.getSenderId(), acceptRequestResponse.getBody().getSenderId());
        Assert.assertEquals("Post accepting request receiverIds are not equal", conversationRequestDto.getReceiverId(), acceptRequestResponse.getBody().getReceiverId());
        Assert.assertEquals("Post accepting request status are not equal",RequestStatus.ACCEPTED.toString(), acceptRequestResponse.getBody().getStatus());

        AcceptConversationRequest conversationRequest = createAcceptConversationRequest(conversationRequestResponse.getBody().getRequestId(), isAccepted);

        headers.setBearerAuth(token2);

        HttpEntity<AcceptConversationRequest> acceptConversationRequestHttpEntity2 =
                new HttpEntity<>(conversationRequest, headers);
        ResponseEntity<String> rejectRequestResponse2 = sendPostRequest(acceptConversationRequestUrl, acceptConversationRequestHttpEntity2, String.class);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = mapper.readValue(rejectRequestResponse2.getBody(), ErrorResponse.class);
        Assert.assertEquals("Status codes are not equal.", HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        Assert.assertNotNull(errorResponse.getMessage());
    }

    private  User createUser()
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

    private ConversationRequestDto createConversationRequest(final String senderId, final String receiverId, final RequestStatus requestStatus)
    {
        ConversationRequestDto conversationRequestDto = new ConversationRequestDto();
        conversationRequestDto.setSenderId(senderId);
        conversationRequestDto.setReceiverId(receiverId);
        conversationRequestDto.setStatus(requestStatus);

        return conversationRequestDto;
    }

    private LoginRequest createLoginRequest(final String username, final String password)
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName(username);
        loginRequest.setPassword(password);

        return loginRequest;
    }

    private HttpHeaders createHttpHeaders(final MediaType mediaType, final String token)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        return headers;
    }

    private <T>ResponseEntity<T> sendPostRequest(String url, Object request, Class<T> responseType)
    {
       return restTemplate.postForEntity(
                url,
                request,
                responseType);
    }

    private AcceptConversationRequest createAcceptConversationRequest(final String requestId, final boolean isAccepted)
    {
        AcceptConversationRequest acceptConversationRequest = new AcceptConversationRequest();
        acceptConversationRequest.setRequestId(requestId);
        acceptConversationRequest.setIsAccepted(isAccepted);

        return acceptConversationRequest;
    }

    private String getLoginURL()
    {
        return "/api/auth/login";
    }

    private String getConversationRequestURL()
    {
        return "/api/conversation/request";
    }
}
