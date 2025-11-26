package com.example.opaybanking.controller;

import com.example.opaybanking.dto.LoginOtpRequest;
import com.example.opaybanking.dto.LoginResponse;
import com.example.opaybanking.dto.RegistrationResponse;
import com.example.opaybanking.model.*;
import com.example.opaybanking.repo.userRepo;
import com.example.opaybanking.service.OtpService;
import com.example.opaybanking.service.userService;
import com.example.opaybanking.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final userService userService;
    private final OtpService otpService;
    private final userRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public AuthController(com.example.opaybanking.service.userService userService, OtpService otpService, com.example.opaybanking.repo.userRepo userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.otpService = otpService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
    @PostMapping("/login/init")
    public ResponseEntity<?> loginInit(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Invalid email or password"));
        }

        // Generate & store OTP
        String otp = otpService.generateOtp(email);
        System.out.println("LOGIN OTP for " + email + ": " + otp);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent! Check Railway logs.",
                "email", email
        ));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");

        if (!otpService.verifyOtp(email, otp)) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Invalid or expired OTP"));
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            user.setAccountStatus(AccountStatus.VERIFIED);
            userRepo.save(user);
        }

        String token = jwtUtil.generateToken(
                String.valueOf(user.getUserId()),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(new LoginResponse(user, token));
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