package com.example.opaybanking.dto;

public record LoginOtpRequest(String email, String password, String otp) {}