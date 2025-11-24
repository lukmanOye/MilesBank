package com.example.opaybanking.dto;

public record InternalTransferRequest(
        String currency,
        String toAccountNumber,
        Double amount,
        String pin,
        String description
) {}