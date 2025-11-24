package com.example.opaybanking.dto;

import com.example.opaybanking.model.User;

public record LoginResponse(boolean success,String message,User user, String token) {
    public LoginResponse(User user, String token) {
        this(true, "Login successful", user,token);
    }
}