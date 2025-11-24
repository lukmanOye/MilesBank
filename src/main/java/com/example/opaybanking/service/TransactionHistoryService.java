package com.example.opaybanking.service;

import com.example.opaybanking.dto.TransactionHistoryResponse;
import com.example.opaybanking.model.*;
import com.example.opaybanking.repo.billPaymentRepo;
import com.example.opaybanking.repo.TransactionRepo;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionHistoryService {

    private final TransactionRepo transactionRepo;
    private final billPaymentRepo billPaymentRepo;
    private final userService userService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public TransactionHistoryService(TransactionRepo transactionRepo,
                                     billPaymentRepo billPaymentRepo,
                                     userService userService) {
        this.transactionRepo = transactionRepo;
        this.billPaymentRepo = billPaymentRepo;
        this.userService = userService;
    }

    public List<TransactionHistoryResponse> getAllHistory(String token) {
        User user = userService.getAuthenticatedUser(token);
        List<TransactionHistoryResponse> history = new ArrayList<>();

        List<Transaction> transfers = transactionRepo
                .findByUserUserIdOrderByCreatedAtDesc(user.getUserId().longValue());
        transfers.forEach(tx -> history.add(mapTransfer(tx)));

        List<BillPayment> bills = billPaymentRepo
                .findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        bills.forEach(bill -> history.add(mapBill(bill)));


        history.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));

        return history;
    }

    public List<TransactionHistoryResponse> getHistoryByMonth(String token, String monthYear) {
        User user = userService.getAuthenticatedUser(token);
        YearMonth ym = parseMonthYear(monthYear);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<TransactionHistoryResponse> history = new ArrayList<>();

        List<Transaction> transfers = transactionRepo
                .findByUserUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        user.getUserId().longValue(), start, end);
        transfers.forEach(tx -> history.add(mapTransfer(tx)));

        List<BillPayment> bills = billPaymentRepo
                .findByUserUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(user.getUserId(), start, end);
        bills.forEach(bill -> history.add(mapBill(bill)));

        history.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));

        return history;
    }

    public TransactionHistoryResponse getHistoryById(String token, Long id, String type) {
        User user = userService.getAuthenticatedUser(token);

        return switch (type.toUpperCase()) {
            case "TRANSFER" -> {
                Transaction tx = transactionRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));
                if (!tx.getUser().getUserId().equals(user.getUserId()))
                    throw new RuntimeException("Access denied");
                yield mapTransfer(tx);
            }
            case "AIRTIME", "DATA", "TV", "ELECTRICITY" -> {
                BillPayment bill = billPaymentRepo.findById(id.intValue())
                        .orElseThrow(() -> new RuntimeException("Bill payment not found"));
                if (!bill.getUser().getUserId().equals(user.getUserId()))
                    throw new RuntimeException("Access denied");
                yield mapBill(bill);
            }
            default -> throw new RuntimeException("Invalid type: " + type);
        };
    }

    private TransactionHistoryResponse mapTransfer(Transaction tx) {
        String desc = tx.getDescription() != null && !tx.getDescription().isBlank()
                ? tx.getDescription()
                : switch (tx.getTransactionType()) {
            case TRANSFER_OUT -> "Sent to " + tx.getBeneficiaryName();
            case TRANSFER_IN -> "Received from " + tx.getWallet().getAccountName();
            case CURRENCY_EXCHANGE_OUT, CURRENCY_EXCHANGE_IN -> "Currency Exchange";
            default -> tx.getTransactionType().name();
        };

        return new TransactionHistoryResponse(
                (long) tx.getTransactionId(),
                "TRANSFER",
                desc,
                tx.getAmount(),
                tx.getTransactionStatus().name(),
                tx.getReference(),
                tx.getBeneficiaryName(),
                DATE_FMT.format(tx.getCreatedAt()),
                tx.getBeneficiaryBank(),
                null,
                null,
                tx.getCreatedAt()
        );
    }

    private TransactionHistoryResponse mapBill(BillPayment bill) {
        String beneficiary = bill.getPhoneNumber() != null ? bill.getPhoneNumber() : bill.getPlanId();
        String desc = bill.getDetails() != null ? bill.getDetails() : bill.getBillType() + " Payment";

        return new TransactionHistoryResponse(
                bill.getBillPaymentId().longValue(),
                bill.getBillType().name(),
                desc,
                bill.getAmount(),
                bill.getTransactionStatus().name(),
                bill.getReference(),
                beneficiary,
                DATE_FMT.format(bill.getCreatedAt()),
                null,
                bill.getNetwork(),
                bill.getPlanId(),
                bill.getCreatedAt()
        );
    }

    private YearMonth parseMonthYear(String input) {
        try {
            if (input.matches("\\d{4}-\\d{2}")) {
                return YearMonth.parse(input);
            } else {
                return YearMonth.parse(input.toUpperCase(), DateTimeFormatter.ofPattern("MMMM yyyy"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid format. Use '2025-11' or 'November 2025'");
        }
    }
}