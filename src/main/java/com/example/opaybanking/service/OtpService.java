package com.example.opaybanking.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private final Map<String, OtpDetails> otpCache = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String generateOtp(String email) {
        String key = email.toLowerCase();
        String otp = String.format("%06d", random.nextInt(1000000));
        otpCache.put(key, new OtpDetails(otp, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
        System.out.println("=== OTP GENERATED ===");
        System.out.println("Email: " + email);
        System.out.println("Key: " + key);
        System.out.println("OTP: " + otp);
        System.out.println("Expires in: 10 minutes");
        System.out.println("=====================");
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = email.toLowerCase();
        OtpDetails details = otpCache.get(key);
        if (details == null) {
            System.out.println("=== OTP VERIFICATION FAILED ===");
            System.out.println("Email: " + email);
            System.out.println("Key: " + key);
            System.out.println("Reason: OTP not found");
            System.out.println("===============================");
            return false;
        }

        if (System.currentTimeMillis() > details.expiryTime) {
            otpCache.remove(key);
            System.out.println("=== OTP VERIFICATION FAILED ===");
            System.out.println("Email: " + email);
            System.out.println("Reason: OTP expired");
            System.out.println("Stored OTP: " + details.otp);
            System.out.println("===============================");
            return false;
        }

        boolean isValid = otp.equals(details.otp);
        if (isValid) {
            otpCache.remove(key);
            System.out.println("=== OTP VERIFICATION SUCCESS ===");
            System.out.println("Email: " + email);
            System.out.println("OTP: " + otp);
            System.out.println("================================");
        } else {
            System.out.println("=== OTP VERIFICATION FAILED ===");
            System.out.println("Email: " + email);
            System.out.println("Reason: OTP mismatch");
            System.out.println("Input OTP: " + otp);
            System.out.println("Stored OTP: " + details.otp);
            System.out.println("===============================");
        }
        return isValid;
    }

    public String getOtp(String email) {
        String key = email.toLowerCase();
        OtpDetails details = otpCache.get(key);
        return (details != null && System.currentTimeMillis() <= details.expiryTime) ? details.otp : null;
    }

    private static class OtpDetails {
        String otp;
        long expiryTime;

        OtpDetails(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}