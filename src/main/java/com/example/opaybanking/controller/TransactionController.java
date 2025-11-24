package com.example.opaybanking.controller;

import com.example.opaybanking.dto.*;
import com.example.opaybanking.enums.Role;
import com.example.opaybanking.model.Bank;
import com.example.opaybanking.model.Transaction;
import com.example.opaybanking.model.User;
import com.example.opaybanking.repo.BankRepo;
import com.example.opaybanking.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ExchangeRateService exchangeRateService;
    private final NameEnquiryService nameEnquiryService;
    private final BankService bankService;
    private final userService userService;
    private final WalletService walletService;

    public TransactionController(TransactionService transactionService, ExchangeRateService exchangeRateService,
                                 NameEnquiryService nameEnquiryService, BankService bankService,
                                 userService userService, WalletService walletService) {
        this.transactionService = transactionService;
        this.exchangeRateService = exchangeRateService;
        this.nameEnquiryService = nameEnquiryService;
        this.bankService = bankService;
        this.userService = userService;
        this.walletService = walletService;
    }


    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Bearer token required");
        }
        return header.substring(7);
    }


    @PostMapping("/verify-name")
    public ResponseEntity<?> verify(@RequestBody VerifyNameRequest req) {
        try {
            if (!bankService.isValidBankCode(req.bankCode())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Invalid bank code");
                response.put("accountNumber", req.accountNumber());
                response.put("bankCode", req.bankCode());
                response.put("message", "The bank code you provided is not valid. Please check and try again.");
                return ResponseEntity.badRequest().body(response);
            }

            if ("190909".equals(req.bankCode())) {
                return ResponseEntity.ok(walletService.verifyMilesBankAccount(req.accountNumber()));
            }

            NameEnquiryResponse enquiryResponse = nameEnquiryService.verifyAccount(req.accountNumber(), req.bankCode());

            Map<String, Object> response = new HashMap<>();
            response.put("success", enquiryResponse.isSuccess());
            response.put("accountName", enquiryResponse.getAccountName());
            response.put("accountNumber", enquiryResponse.getAccountNumber());
            response.put("bankCode", enquiryResponse.getBankCode());
            response.put("message", enquiryResponse.getMessage());

            if (enquiryResponse.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    @GetMapping("/banks")
    public ResponseEntity<?> getAllBanks() {
        return ResponseEntity.ok(bankService.getAllBanks());
    }
    @PostMapping("/internal")
    public ResponseEntity<?> internalTransfer(@RequestBody InternalTransferRequest req,
                                              @RequestHeader("Authorization") String auth) {
        return handle(() -> transactionService.internalTransfer(req, extractToken(auth)));
    }

    @PostMapping("/cross-currency")
    public ResponseEntity<?> crossCurrencyTransfer(@RequestBody CrossCurrencyTransferRequest req,
                                                   @RequestHeader("Authorization") String auth) {
        return handle(() -> transactionService.crossCurrencyTransfer(req, extractToken(auth)));
    }

    @PostMapping("/external")
    public ResponseEntity<?> externalTransfer(@RequestBody ExternalTransferRequest req,
                                              @RequestHeader("Authorization") String auth) {
        return handle(() -> transactionService.externalTransfer(req, extractToken(auth)));
    }


    @GetMapping("/my")
    public ResponseEntity<?> myTransactions(@RequestHeader("Authorization") String auth) {
        return handle(() -> transactionService.getUserTransactions(extractToken(auth)));
    }

    @GetMapping("/my/filter")
    public ResponseEntity<?> filterTransactions(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "date") String sort) {

        return handle(() -> {
            String token = extractToken(auth);
            return switch (sort.toLowerCase()) {
                case "amount" -> transactionService.getUserTransactionsByAmountDesc(token);
                case "date"   -> transactionService.getUserTransactionsByDateRange(token, start, end);
                default       -> throw new RuntimeException("Sort must be 'date' or 'amount'");
            };
        });
    }

    @GetMapping("/my/month")
    public ResponseEntity<?> getTransactionsByMonth(
            @RequestHeader("Authorization") String auth,
            @RequestParam String month) {
        return handle(() -> transactionService.getTransactionsByMonth(extractToken(auth), month));
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable Long id,
                                            @RequestHeader("Authorization") String auth) {
        return handle(() -> transactionService.getTransaction(id, extractToken(auth)));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<?> walletTransactions(@PathVariable Long walletId,
                                                @RequestHeader("Authorization") String auth) {
        return handle(() -> transactionService.getWalletTransactions(walletId, extractToken(auth)));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllTransactions(@RequestHeader("Authorization") String auth) {
        String token = extractToken(auth);
        User user = userService.getAuthenticatedUser(token);
        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Admin access required"));
        }
        return handle(() -> transactionService.getAllTransactions(token));
    }


    @GetMapping("/rates")
    public ResponseEntity<?> getRates() {
        return ResponseEntity.ok(exchangeRateService.getAllRates());
    }

    @PutMapping("/admin/rates")
    public ResponseEntity<?> updateRates(@RequestBody Map<String, Double> rates,
                                         @RequestHeader("Authorization") String auth) {
        String token = extractToken(auth);
        User user = userService.getAuthenticatedUser(token);
        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Admin access required"));
        }
        try {
            exchangeRateService.updateRate(rates);
            return ResponseEntity.ok(Map.of("message", "Exchange rates updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<?> handle(Supplier<Object> supplier) {
        try {
            return ResponseEntity.ok(supplier.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @FunctionalInterface
    interface Supplier<T> {
        T get() throws Exception;
    }
}