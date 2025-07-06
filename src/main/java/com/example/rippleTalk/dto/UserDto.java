package com.example.rippleTalk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;              // UUID as a string
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;          // e.g., ACTIVE, BANNED
}
