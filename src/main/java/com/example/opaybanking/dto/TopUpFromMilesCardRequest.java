package com.example.opaybanking.dto;

import com.example.opaybanking.enums.Currency;

public record TopUpFromMilesCardRequest(
        String cardNumber,
        String expiry,
        String cvv,
        String pin,
        double amount,
        Currency currency
) {}