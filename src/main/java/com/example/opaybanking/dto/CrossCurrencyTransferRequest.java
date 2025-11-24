package com.example.opaybanking.dto;

public record CrossCurrencyTransferRequest(
        String currency,
        String toAccountNumber,
        Double amount,
        String pin
) {}