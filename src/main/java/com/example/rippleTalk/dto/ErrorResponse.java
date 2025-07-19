package com.example.rippleTalk.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ErrorResponse
{
    private final int status;
    private final String error;
    private final String message;
    private final String timestamp;

    public ErrorResponse(int status, String error, String message, String timestamp)
    {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }
}
