package com.example.rippleTalk.constants;

public final class ApiEndpoints {

    private ApiEndpoints() {
        // Prevent instantiation
    }

    public static final String AUTH_BASE = "/api/auth";
    public static final String REGISTER = AUTH_BASE + "/register";

    public static final String[] PUBLIC_ENDPOINTS = {
            REGISTER
    };
}
