package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.dto.RegisterRequest;
import com.example.rippleTalk.dto.UserDto;
import com.example.rippleTalk.exception.BadRequestException;
import com.example.rippleTalk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    private final AuthService authService;

    public AuthController(final AuthService authService)
    {
        this.authService = authService;
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getUserName() == null || request.getUserName().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Username and Password must not be null or blank");
        }
        System.out.println("Login endpoint hit");
        String token = authService.authenticate(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
