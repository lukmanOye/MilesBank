package com.example.opaybanking.dto;

public record BillPaymentResponse(
        boolean success,
        String message,
        String transactionId,
        double amount,
        String details
) {
    public static BillPaymentResponse success(String message, String transactionId, double amount, String details) {
        return new BillPaymentResponse(true, message, transactionId, amount, details);
    }

    public static BillPaymentResponse error(String message) {
        return new BillPaymentResponse(false, message, null, 0, null);
    }
}