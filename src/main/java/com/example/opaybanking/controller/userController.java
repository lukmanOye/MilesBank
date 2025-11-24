package com.example.opaybanking.controller;

import com.example.opaybanking.dto.LoginResponse;
import com.example.opaybanking.model.User;
import com.example.opaybanking.repo.userRepo;
import com.example.opaybanking.service.CloudinaryService;
import com.example.opaybanking.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class userController {

    private final userService userService;
    private final CloudinaryService cloudinaryService;
    private final userRepo userRepo;

    public userController(userService userService,
                          CloudinaryService cloudinaryService, userRepo userRepo) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.userRepo = userRepo;
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Bearer token required");
        }
        return authHeader.substring(7);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<User> users = userService.getAllUsers(token);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "403");
            errorResponse.put("message", "Access denied - Admin role required");
            return ResponseEntity.status(403).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            User user = userService.getUserById(id, token);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "404");
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id,
                                        @RequestBody User user,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            User updatedUser = userService.updateUser(id, user, token);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "403");
            return ResponseEntity.status(403).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            userService.deleteUser(id, token);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "403");
            errorResponse.put("message", "Access denied - Admin role required");
            return ResponseEntity.status(403).body(errorResponse);
        }
    }

    @PutMapping("/update-profile-picture")
    public ResponseEntity<?> updateProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String auth) {

        String token = auth.substring(7);
        User user = userService.getAuthenticatedUser(token);

        try {
            String imageUrl = cloudinaryService.upload(file);
            user.setProfilePictureUrl(imageUrl);
            userRepo.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile picture updated",
                    "profilePictureUrl", imageUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Upload failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            User currentUser = userService.getAuthenticatedUser(token);
            return ResponseEntity.ok(new LoginResponse(currentUser,token));
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "401");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }


    @GetMapping("/my-role")
    public ResponseEntity<?> getMyRole(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            User currentUser = userService.getAuthenticatedUser(token);
            Map<String, String> response = new HashMap<>();
            response.put("email", currentUser.getEmail());
            response.put("role", currentUser.getRole().name());
            response.put("userId", String.valueOf(currentUser.getUserId()));
            response.put("status", "active");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "401");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}