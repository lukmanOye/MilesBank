package com.example.opaybanking.dto;


public record ExternalTransferRequest(
        String accountNumber,
        String accountName,
        String bankCode,
        Double amount,
        String pin,
        String currency,
        String description
) {
    public ExternalTransferRequest {
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name is required");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank code is required");
        }
    }
}