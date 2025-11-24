package com.example.opaybanking.dto;

import com.example.opaybanking.enums.BillType;

public record BillPaymentRequest(
        BillType billType,
        String phoneNumber,
        String network,
        String planId,
        String meterNumber,
        String tvProvider,
        double amount) {
}
