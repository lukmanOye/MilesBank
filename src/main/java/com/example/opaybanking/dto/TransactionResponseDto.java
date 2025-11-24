package com.example.opaybanking.dto;

public record TransactionResponseDto(
        Long transactionId,
        String transactionType,
        String reference,
        Double amount,
        String beneficiaryName,
        String beneficiaryAccount,
        String beneficiaryBank,
        String description,
        String status,                 // SUCCESSFUL, PENDING
        String date                   // "20 Nov 2025, 02:41 PM"
) {}