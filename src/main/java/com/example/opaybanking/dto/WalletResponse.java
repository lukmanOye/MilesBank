package com.example.opaybanking.dto;

import com.example.opaybanking.model.Wallet;
import java.time.LocalDateTime;

public record WalletResponse(
        Integer walletId,
        String accountNumber,
        String accountName,
        double balance,
        String currency,
        LocalDateTime createdAt,
        Integer userId,
        String userFullName,
        String bankName,
        String bankCode
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                Math.toIntExact(wallet.getWalletId()),
                wallet.getAccountNumber(),
                wallet.getAccountName(),
                wallet.getBalance(),
                wallet.getCurrency().name(),
                wallet.getCreatedAt(),
                wallet.getUser() != null ? wallet.getUser().getUserId() : null,
                wallet.getUser() != null
                        ? wallet.getUser().getFirstName() + " " + wallet.getUser().getLastName()
                        : null,
                wallet.getBank() != null ? wallet.getBank().getBankName() : null,
                wallet.getBank() != null ? wallet.getBank().getBankCode() : null
        );
    }
}