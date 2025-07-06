package com.example.rippleTalk.controller;

import com.example.rippleTalk.dto.RegisterRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    @GetMapping("/message")
    public String getMessage()
    {
        return "Hello World";
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterRequest request)
    {
        System.out.println(request.toString());
        return null;
    }
}
