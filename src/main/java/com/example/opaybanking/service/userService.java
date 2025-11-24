package com.example.opaybanking.service;

import com.example.opaybanking.dto.LoginOtpRequest;
import com.example.opaybanking.dto.LoginResponse;
import com.example.opaybanking.dto.RegistrationResponse;
import com.example.opaybanking.enums.Role;
import com.example.opaybanking.model.*;
import com.example.opaybanking.repo.userRepo;
import com.example.opaybanking.util.JwtUtil;
import com.example.opaybanking.util.TokenBlacklist;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Service
public class userService {

    private final userRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    @Autowired
    public userService(userRepo userRepo, PasswordEncoder passwordEncoder,
                       EmailService emailService, OtpService otpService,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpService = otpService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    public RegistrationResponse createUser(User user) {
        try {
            System.out.println("=== REGISTRATION STARTED ===");
            System.out.println("Incoming Role: " + user.getRole());

            // Validate required
            if (user.getEmail() == null || user.getEmail().isBlank())
                throw new RuntimeException("Email is required");
            if (userRepo.findByEmailIgnoreCase(user.getEmail()) != null)
                throw new RuntimeException("Email already exists");
            if (user.getFirstName() == null || user.getFirstName().isBlank())
                throw new RuntimeException("First name required");
            if (user.getPassword() == null || user.getPassword().isBlank())
                throw new RuntimeException("Password required");

            // Encrypt password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // CRITICAL: ONLY SET DEFAULT ROLE IF NULL
            if (user.getRole() == null) {
                user.setRole(Role.USER);
                System.out.println("Role was null → Set to USER");
            } else {
                System.out.println("Role received from request → " + user.getRole());
            }

            user.setAccountStatus(AccountStatus.PENDING);

            User saved = userRepo.save(user);
            String otp = otpService.generateOtp(saved.getEmail());

            try {
                emailService.sendWelcomeEmail(saved.getEmail(), saved.getFirstName());
                emailService.sendOtpEmail(saved.getEmail(), otp);
            } catch (Exception e) {
                System.out.println("Email failed (non-blocking): " + e.getMessage());
            }

            System.out.println("=== REGISTRATION SUCCESS ===");
            System.out.println("SAVED USER ROLE: " + saved.getRole());

            return new RegistrationResponse(
                    "Registration successful! Check your email for OTP.",
                    "http://localhost:8080/api/auth/login",
                    otp,
                    saved.getEmail()
            );

        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    public LoginResponse loginWithOtp(LoginOtpRequest req) {
        System.out.println("=== LOGIN ATTEMPT STARTED ===");
        System.out.println("Email: " + req.email());
        System.out.println("OTP Provided: " + req.otp());
        System.out.println("Password Provided: " + (req.password() != null ? "***" : "null"));

        try {
            User user = userRepo.findByEmailIgnoreCase(req.email());
            if (user == null) {
                System.out.println("=== LOGIN FAILED: USER NOT FOUND ===");
                throw new RuntimeException("Invalid email or password");
            }

            System.out.println("User found: " + user.getEmail());
            System.out.println("User status: " + user.getAccountStatus());

            if (!otpService.verifyOtp(req.email(), req.otp())) {
                System.out.println("=== LOGIN FAILED: INVALID OTP ===");
                throw new RuntimeException("Invalid or expired OTP. Please check your OTP and try again.");
            }

            System.out.println("OTP verified successfully");

            if (user.getAccountStatus() == AccountStatus.PENDING) {
                user.setAccountStatus(AccountStatus.VERIFIED);
                user = userRepo.save(user);
                System.out.println("Account status upgraded to VERIFIED");
            }

            try {
                System.out.println("Authenticating password...");
                Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.email(), req.password())
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("Password authentication successful");
            } catch (Exception e) {
                System.out.println("=== LOGIN FAILED: PASSWORD AUTHENTICATION FAILED ===");
                System.out.println("Authentication error: " + e.getMessage());
                System.out.println("Error type: " + e.getClass().getSimpleName());
                throw new RuntimeException("Invalid email or password: " + e.getMessage());
            }

            String token = jwtUtil.generateToken(
                    String.valueOf(user.getUserId()),
                    user.getEmail(),
                    user.getRole().name()
            );

            System.out.println("=== LOGIN SUCCESSFUL ===");
            System.out.println("User: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Token generated successfully");

            return new LoginResponse(user, token);

        } catch (RuntimeException e) {
            System.out.println("=== LOGIN PROCESS FAILED ===");
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    public RegistrationResponse resendOtp(String email) {
        System.out.println("=== RESEND OTP REQUEST ===");
        System.out.println("Email: " + email);

        try {
            User user = userRepo.findByEmailIgnoreCase(email);
            if (user == null) {
                System.out.println("=== RESEND OTP FAILED: USER NOT FOUND ===");
                throw new RuntimeException("Email not registered");
            }

            String otp = otpService.generateOtp(email);
            System.out.println("New OTP generated: " + otp);

            try {
                emailService.sendOtpEmail(email, otp);
                System.out.println("OTP email sent successfully");
            } catch (MessagingException e) {
                System.out.println("=== EMAIL SENDING FAILED ===");
                System.out.println("Error: " + e.getMessage());
            }

            return new RegistrationResponse(
                    "New OTP sent successfully!",
                    "http://localhost:8080/api/auth/login",
                    otp,
                    email
            );

        } catch (RuntimeException e) {
            System.out.println("=== RESEND OTP FAILED ===");
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to resend OTP: " + e.getMessage());
        }
    }


    public void logout(String token) {
        System.out.println("=== LOGOUT REQUEST ===");
        try {
            String jti = jwtUtil.getJti(token);
            tokenBlacklist.blacklistToken(jti);
            System.out.println("Token blacklisted successfully");
        } catch (Exception e) {
            System.out.println("=== LOGOUT FAILED ===");
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    public User getAuthenticatedUser(String token) {
        try {
            String jti = jwtUtil.getJti(token);
            if (tokenBlacklist.isBlacklisted(jti)) {
                throw new RuntimeException("Token has been logged out");
            }
            String email = jwtUtil.getEmail(token);
            User user = userRepo.findByEmailIgnoreCase(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token: " + e.getMessage());
        }
    }

    public List<User> getAllUsers(String token) {
        System.out.println("=== GET ALL USERS REQUEST ===");
        User user = getAuthenticatedUser(token);

        if (user.getRole() != Role.ADMIN) {
            System.out.println("=== ACCESS DENIED: USER IS NOT ADMIN ===");
            System.out.println("User role: " + user.getRole() + ", Required: ADMIN");
            throw new RuntimeException("Admin access required. Your role: " + user.getRole());
        }

        List<User> users = userRepo.findAll();
        System.out.println("Returning " + users.size() + " users");
        return users;
    }

    public User getUserById(int id, String token) {
        System.out.println("=== GET USER BY ID REQUEST ===");
        System.out.println("User ID: " + id);
        getAuthenticatedUser(token);
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        System.out.println("User found: " + user.getEmail());
        return user;
    }

    public User updateUser(int id, User user, String token) {
        System.out.println("=== UPDATE USER REQUEST ===");
        System.out.println("User ID: " + id);

        User authUser = getAuthenticatedUser(token);
        if (authUser.getUserId() != id && authUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied. You can only update your own profile.");
        }

        User existing = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (user.getFirstName() != null) existing.setFirstName(user.getFirstName());
        if (user.getLastName() != null) existing.setLastName(user.getLastName());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getPhoneNumber() != null) existing.setPhoneNumber(user.getPhoneNumber());
        if (user.getAddress() != null) existing.setAddress(user.getAddress());

        User updated = userRepo.save(existing);

        try {
            emailService.sendUpdateNotification(updated.getEmail(), updated.getFirstName());
            System.out.println("Update notification email sent");
        } catch (MessagingException e) {
            System.out.println("Failed to send update notification: " + e.getMessage());
        }

        System.out.println("=== USER UPDATED SUCCESSFULLY ===");
        return updated;
    }


    public void deleteUser(int id, String token) {
        System.out.println("=== DELETE USER REQUEST ===");
        System.out.println("User ID: " + id);

        User authUser = getAuthenticatedUser(token);
        if (authUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Admin access required. Your role: " + authUser.getRole());
        }

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        userRepo.delete(user);
        System.out.println("=== USER DELETED SUCCESSFULLY ===");
    }


}