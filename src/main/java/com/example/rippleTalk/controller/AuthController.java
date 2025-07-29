package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.dto.RegisterRequest;
import com.example.rippleTalk.dto.UserDto;
import com.example.rippleTalk.exception.BadRequestException;
import com.example.rippleTalk.exception.RateLimitExceededException;
import com.example.rippleTalk.service.AuthService;
import com.example.rippleTalk.service.LoginRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    private final AuthService authService;
    private final LoginRateLimiterService rateLimiterService;

    public AuthController(final AuthService authService, final LoginRateLimiterService rateLimiterService)
    {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/message")
    public String getMessage()
    {
        return "Hello World";
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody RegisterRequest request)
    {
        UserDto createdUser = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest)
    {
        if (request.getUserName() == null || request.getUserName().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Username and Password must not be null or blank");
        }
        
        String userKey = request.getUserName();
        String clientIp = extractClientIp(servletRequest);

        if (rateLimiterService.isIpLimitExceeded(clientIp)) {
            throw new RateLimitExceededException("Too many unique IPs in time window");
        }

        if (!rateLimiterService.tryConsumeUser(userKey))
        {
            throw new RateLimitExceededException("Too many login attempts for user: " + userKey);
        }

        if (!rateLimiterService.tryConsumeIp(clientIp)) {
            throw new RateLimitExceededException("Too many login attempts from IP: " + clientIp);
        }

        System.out.println("Login endpoint hit");
        String token = authService.authenticate(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private String extractClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp))
        {
            clientIp = request.getRemoteAddr();
        }

        if (clientIp != null && clientIp.contains(","))
        {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }
}
