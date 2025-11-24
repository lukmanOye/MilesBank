package com.example.opaybanking.controller;

import com.example.opaybanking.model.ExchangeRate;
import com.example.opaybanking.model.User;
import com.example.opaybanking.service.ExchangeRateService;
import com.example.opaybanking.service.userService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/rates")
public class ExchangeRateController {

    private final ExchangeRateService rateService;
    private final userService userService;

    public ExchangeRateController(ExchangeRateService rateService, userService userService) {
        this.rateService = rateService;
        this.userService = userService;
    }

    private String token(String auth) {
        return auth.substring(7);
    }

    private User requireAdmin(String token) {
        User user = userService.getAuthenticatedUser(token);
        if (user.getRole() != com.example.opaybanking.enums.Role.ADMIN) {
            throw new RuntimeException("Access denied. Admin only.");
        }
        return user;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRates(@RequestHeader("Authorization") String auth) {

        userService.getAuthenticatedUser(token(auth));
        return ResponseEntity.ok(rateService.getAllRates());
    }

    @PostMapping
    public ResponseEntity<ExchangeRate> createRate(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Double> rates) {

        requireAdmin(token(auth));
        ExchangeRate rate = rateService.updateRate(rates);
        return ResponseEntity.ok(rate);
    }

    @PutMapping
    public ResponseEntity<ExchangeRate> updateRate(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Double> rates) {

        requireAdmin(token(auth));
        ExchangeRate updated = rateService.updateRate(rates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllRates(@RequestHeader("Authorization") String auth) {
        requireAdmin(token(auth));
        rateService.deleteRate(1L);
        return ResponseEntity.ok("Exchange rates reset. New default created on next request.");
    }

    @GetMapping("/raw")
    public ResponseEntity<ExchangeRate> getRawRate(@RequestHeader("Authorization") String auth) {
        requireAdmin(token(auth));
        return ResponseEntity.ok(rateService.getRate());
    }
}