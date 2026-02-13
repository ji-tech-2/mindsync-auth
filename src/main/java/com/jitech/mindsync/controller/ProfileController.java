package com.jitech.mindsync.controller;

import com.jitech.mindsync.dto.*;
import com.jitech.mindsync.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Get current user's profile
     * Requires: JWT cookie (automatically handled by JwtAuthenticationFilter)
     */
    @GetMapping
    public ResponseEntity<?> getProfile() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
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
     * Requires: JWT cookie (automatically handled by JwtAuthenticationFilter)
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
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
}
