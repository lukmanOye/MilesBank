
package com.example.opaybanking.dto;

public record CreateCardResponse(
        boolean success,
        String message,
        String cardNumber,
        String expiry,
        String cvv,
        String pin,
        String type,
        String currency,
        String username         // Full name: David King
) {}