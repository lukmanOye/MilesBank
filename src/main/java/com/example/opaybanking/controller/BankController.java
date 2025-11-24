package com.example.opaybanking.controller;

import com.example.opaybanking.enums.Role;
import com.example.opaybanking.model.Bank;
import com.example.opaybanking.model.User;
import com.example.opaybanking.service.BankService;
import com.example.opaybanking.service.userService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banks")
public class BankController {

    private final BankService bankService;
    private final userService userService;

    public BankController(BankService bankService, userService userService) {
        this.bankService = bankService;
        this.userService = userService;
    }

    private String token(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth;
    }

    private void requireAdmin(String auth) {
        User user = userService.getAuthenticatedUser(token(auth));
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Admin access required");
        }
    }

    @GetMapping
    public ResponseEntity<List<Bank>> getAllBanks() {
        return ResponseEntity.ok(bankService.getAllBanks());
    }

    @GetMapping("/code/{bankCode}")
    public ResponseEntity<?> getBankByCode(@PathVariable String bankCode) {
        return bankService.findByCode(bankCode)
                .map(bank -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "bankCode", bank.getBankCode(),
                        "bankName", bank.getBankName(),
                        "isMilesBank", "190909".equals(bank.getBankCode())
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "success", false,
                        "bankCode", bankCode,
                        "bankName", "Unknown Bank",
                        "message", "Bank not found or not supported"
                )));
    }

    @GetMapping("/validate/{bankCode}")
    public ResponseEntity<Map<String, Object>> validateBankCode(@PathVariable String bankCode) {
        boolean valid = bankService.isValidBankCode(bankCode);
        String name = valid ? bankService.resolveBankName(bankCode) : "Unknown Bank";

        return ResponseEntity.ok(Map.of(
                "bankCode", bankCode,
                "isValid", valid,
                "bankName", name,
                "isMilesBank", "190909".equals(bankCode)
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Bank>> searchBanks(@RequestParam String q) {
        String query = q.toLowerCase().trim();
        List<Bank> results = bankService.getAllBanks().stream()
                .filter(b -> b.getBankName().toLowerCase().contains(query) ||
                        b.getBankCode().contains(query))
                .toList();
        return ResponseEntity.ok(results);
    }

}