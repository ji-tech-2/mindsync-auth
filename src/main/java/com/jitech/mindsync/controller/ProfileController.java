package com.jitech.mindsync.controller;

import com.jitech.mindsync.dto.*;
import com.jitech.mindsync.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

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
            logger.info("GET /profile - Profile request received for email: {}", email);
            ProfileResponse profile = profileService.getProfile(email);

            logger.info("GET /profile - Profile retrieved successfully for email: {}", email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", profile));
        } catch (IllegalArgumentException e) {
            logger.error("GET /profile - Error: {}", e.getMessage());
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
            logger.info("PUT /profile - Profile update request received for email: {}", email);
            ProfileResponse updatedProfile = profileService.updateProfile(email, request);

            logger.info("PUT /profile - Profile updated successfully for email: {}", email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully",
                    "data", updatedProfile));
        } catch (IllegalArgumentException e) {
            logger.warn("PUT /profile - Update failed: {}", e.getMessage());
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
        logger.info("POST /profile/request-otp - OTP request received for email: {}", request.getEmail());
        try {
            profileService.requestPasswordReset(request.getEmail());

            logger.info("POST /profile/request-otp - OTP request processed successfully for email: {}",
                    request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP has been sent to your email"));
        } catch (IllegalArgumentException e) {
            logger.warn("POST /profile/request-otp - Request failed for email: {}. Reason: {}",
                    request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        } catch (RuntimeException e) {
            // Catches email sending failures (RuntimeException thrown by EmailService)
            logger.error("POST /profile/request-otp - Internal error for email: {}. Error: {}",
                    request.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to send OTP. Please try again later."));
        }
    }

    /**
     * Reset password using OTP
     * Public endpoint - no token required
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("POST /profile/reset-password - Password reset request received for email: {}",
                request.getEmail());
        try {
            boolean success = profileService.resetPassword(
                    request.getEmail(),
                    request.getOtp(),
                    request.getNewPassword());

            if (success) {
                logger.info("POST /profile/reset-password - Password reset successfully for email: {}",
                        request.getEmail());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Password reset successfully"));
            } else {
                logger.warn("POST /profile/reset-password - Invalid OTP for email: {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid or expired OTP. Please request a new one."));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("POST /profile/reset-password - Request failed for email: {}. Reason: {}",
                    request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Change password for authenticated user
     * Requires: JWT cookie (automatically handled by JwtAuthenticationFilter)
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            logger.info("POST /profile/change-password - Password change request received for email: {}", email);

            boolean success = profileService.changePassword(email, request.getOldPassword(), request.getNewPassword());

            if (success) {
                logger.info("POST /profile/change-password - Password changed successfully for email: {}", email);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Password changed successfully"));
            } else {
                logger.warn("POST /profile/change-password - Incorrect old password for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Incorrect old password"));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("POST /profile/change-password - Request failed. Reason: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
