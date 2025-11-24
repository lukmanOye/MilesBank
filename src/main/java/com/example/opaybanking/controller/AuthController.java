package com.example.opaybanking.controller;

import com.example.opaybanking.dto.LoginOtpRequest;
import com.example.opaybanking.dto.LoginResponse;
import com.example.opaybanking.dto.RegistrationResponse;
import com.example.opaybanking.model.*;
import com.example.opaybanking.service.userService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final userService userService;

    public AuthController(com.example.opaybanking.service.userService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            RegistrationResponse response = userService.createUser(user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "400");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginOtpRequest loginRequest) {
        try {
            LoginResponse response = userService.loginWithOtp(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new RegistrationResponse("Login failed: " + e.getMessage(),
                            "http://localhost:8080/api/auth/login",
                            null,
                            loginRequest.email())
            );
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        try {
            userService.resendOtp(email);
            return ResponseEntity.ok().body("OTP resent successfully to " + email);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to resend OTP: " + e.getMessage());
        }
    }



    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String auth) {
        if (auth == null || !auth.startsWith("Bearer "))
            return ResponseEntity.badRequest().body("Bearer token required");
        userService.logout(auth.substring(7));
        return ResponseEntity.ok("Logged out");
    }
}