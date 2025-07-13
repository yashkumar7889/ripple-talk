package com.example.rippleTalk.util;

import java.util.UUID;

public class TestUserUtil {

    public static String generateUniqueTestEmail() {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    public static String generateUniqueUsername() {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
