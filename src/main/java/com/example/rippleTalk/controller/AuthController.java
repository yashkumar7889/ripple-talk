package com.example.rippleTalk.controller;

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

    // Constructor injection
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
}
