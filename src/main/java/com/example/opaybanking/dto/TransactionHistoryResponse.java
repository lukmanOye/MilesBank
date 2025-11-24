package com.example.opaybanking.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        Long id,
        String type,
        String description,
        Double amount,
        String status,
        String reference,
        String beneficiary,
        String date,
        String bank,
        String network,
        String plan,
        LocalDateTime createdAt
) {}