package com.jitech.mindsync.controller;

import com.jitech.mindsync.dto.*;
import com.jitech.mindsync.security.JwtProvider;
import com.jitech.mindsync.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = {
        "http://139.59.109.5",
        "http://165.22.63.100",
        "http://165.22.246.95",
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8080",
        "http://localhost:8081",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:8081"
})
public class ProfileController {

    private final ProfileService profileService;
    private final JwtProvider jwtProvider;

    @Autowired
    public ProfileController(ProfileService profileService, JwtProvider jwtProvider) {
        this.profileService = profileService;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Get current user's profile
     * Requires: Bearer token in Authorization header
     */
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmailFromToken(authHeader);
            ProfileResponse profile = profileService.getProfile(email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Update current user's profile (name, gender, occupation)
     * Requires: Bearer token in Authorization header
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProfileUpdateRequest request) {
        try {
            String email = extractEmailFromToken(authHeader);
            ProfileResponse updatedProfile = profileService.updateProfile(email, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully",
                    "data", updatedProfile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Request OTP for password reset
     * Public endpoint - no token required
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody OtpRequest request) {
        try {
            profileService.requestPasswordReset(request.getEmail());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP has been sent to your email"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        } catch (RuntimeException e) {
            // Catches email sending failures (RuntimeException thrown by EmailService)
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to send OTP. Please try again later."));
        }
    }

    /**
     * Change password using OTP
     * Public endpoint - no token required
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            boolean success = profileService.changePassword(
                    request.getEmail(),
                    request.getOtp(),
                    request.getNewPassword());

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Password changed successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid or expired OTP. Please request a new one."));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7);

        // Validate token before extracting email
        if (!jwtProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        return jwtProvider.getEmailFromToken(token);
    }
}
