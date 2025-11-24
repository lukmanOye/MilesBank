package com.example.opaybanking.dto;

public record TransferResponse(
        boolean success,
        String message,
        String reference,
        String fromAccount,
        String toAccount,
        String beneficiaryName,
        String bankName,
        Double amount,
        Double newBalance
) {}