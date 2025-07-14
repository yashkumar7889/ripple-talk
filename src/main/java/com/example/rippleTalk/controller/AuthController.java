package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.LoginRequest;
import com.example.rippleTalk.dto.LoginResponse;
import com.example.rippleTalk.dto.RegisterRequest;
import com.example.rippleTalk.dto.UserDto;
import com.example.rippleTalk.service.AuthService;
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
        System.out.println("Login endpoint hit");
        String token = authService.authenticate(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
