package com.example.opaybanking.dto;

public record TopUpResponse(boolean success, String message, double amount) {}