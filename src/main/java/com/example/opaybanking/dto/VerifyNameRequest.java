package com.example.opaybanking.dto;

public record VerifyNameRequest(
        String accountNumber,
        String bankName,
        String bankCode
) {}