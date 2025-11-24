package com.example.opaybanking.controller;

import com.example.opaybanking.dto.NameEnquiryResponse;
import com.example.opaybanking.dto.PinUpdateRequest;
import com.example.opaybanking.dto.WalletResponse;
import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.model.Wallet;
import com.example.opaybanking.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing token");
        }
        return authHeader.substring(7);
    }
    private String extract(String h) { return h.startsWith("Bearer ") ? h.substring(7) : h; }

    // Create Wallets
    @PostMapping("/create/ngn")
    public ResponseEntity<?> createNgn(@RequestBody Map<String, String> body,
                                       @RequestHeader("Authorization") String auth) {
        try {
            Wallet wallet = walletService.createNairaWallet(body.get("pin"), extractToken(auth));
            return ResponseEntity.ok(WalletResponse.from(wallet));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create/usd")
    public ResponseEntity<?> createUsd(@RequestBody Map<String, String> body,
                                       @RequestHeader("Authorization") String auth) {
        try {
            Wallet wallet = walletService.createDollarWallet(body.get("pin"), extractToken(auth));
            return ResponseEntity.ok(WalletResponse.from(wallet));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<WalletResponse>> myWallets(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(walletService.getUserWallets(extractToken(auth)));
    }

    @GetMapping("/my/ngn")
    public ResponseEntity<WalletResponse> myNgn(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(WalletResponse.from(walletService.getNairaWallet(extractToken(auth))));
    }

    @GetMapping("/my/usd")
    public ResponseEntity<WalletResponse> myUsd(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(WalletResponse.from(walletService.getDollarWallet(extractToken(auth))));
    }

    @GetMapping("/balance/ngn")
    public ResponseEntity<?> getNgnBalance(@RequestHeader("Authorization") String auth) {
        double bal = walletService.getWalletByCurrency(extract(auth), Currency.NGN).getBalance();
        return ResponseEntity.ok(Map.of("currency", "NGN", "balance", bal));
    }

    @GetMapping("/balance/usd")
    public ResponseEntity<?> getUsdBalance(@RequestHeader("Authorization") String auth) {
        try {
            double bal = walletService.getWalletByCurrency(extract(auth), Currency.USD).getBalance();
            return ResponseEntity.ok(Map.of("currency", "USD", "balance", bal));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("currency", "USD", "balance", 0.0, "note", "USD wallet not created"));
        }
    }

    @GetMapping("/balances")
    public ResponseEntity<?> getAllBalances(@RequestHeader("Authorization") String auth) {
        String token = extract(auth);
        double ngn = walletService.getWalletByCurrency(token, Currency.NGN).getBalance();
        double usd = 0.0;
        try {
            usd = walletService.getWalletByCurrency(token, Currency.USD).getBalance();
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("ngn", ngn, "usd", usd));
    }



    @PutMapping("/pin/ngn")
    public ResponseEntity<?> changeNgnPin(@RequestBody PinUpdateRequest req,
                                          @RequestHeader("Authorization") String auth) {
        try {
            return ResponseEntity.ok(walletService.changeNgnPin(extractToken(auth), req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/pin/usd")
    public ResponseEntity<?> changeUsdPin(@RequestBody PinUpdateRequest req,
                                          @RequestHeader("Authorization") String auth) {
        try {
            return ResponseEntity.ok(walletService.changeUsdPin(extractToken(auth), req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-account")
    public ResponseEntity<NameEnquiryResponse> verifyAccount(@RequestBody Map<String, String> body) {
        String accountNumber = body.get("accountNumber");
        if (accountNumber == null || accountNumber.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(NameEnquiryResponse.failed("Account number is required", "Miles Bank"));
        }

        try {
            NameEnquiryResponse response = walletService.verifyMilesBankAccount(accountNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(NameEnquiryResponse.failed(e.getMessage(), "Miles Bank"));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> adminAllWallets(@RequestHeader("Authorization") String auth) {
        try {
            return ResponseEntity.ok(walletService.getAllWalletsAdmin(extractToken(auth)));
        } catch (Exception e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/balance/{id}")
    public ResponseEntity<?> adminUpdateBalance(@PathVariable Integer id,
                                                @RequestBody Map<String, Double> body,
                                                @RequestHeader("Authorization") String auth) {
        try {
            Double balance = body.get("balance");
            if (balance == null) throw new RuntimeException("Balance is required");
            return ResponseEntity.ok(
                    walletService.adminUpdateBalance(id, balance, extractToken(auth))
            );
        } catch (Exception e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> adminDeleteWallet(@PathVariable Integer id,
                                               @RequestHeader("Authorization") String auth) {
        try {
            walletService.adminDeleteWallet(id, extractToken(auth));
            return ResponseEntity.ok(Map.of("message", "Wallet deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}