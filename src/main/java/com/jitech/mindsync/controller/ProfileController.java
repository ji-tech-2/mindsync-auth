package com.jitech.mindsync.controller;

import com.jitech.mindsync.dto.*;
import com.jitech.mindsync.model.OtpType;
import com.jitech.mindsync.service.OtpService;
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
        private final OtpService otpService;

        @Autowired
        public ProfileController(ProfileService profileService, OtpService otpService) {
                this.profileService = profileService;
                this.otpService = otpService;
        }

        /**
         * Get current user's profile
         * Requires: JWT cookie (automatically handled by JwtAuthenticationFilter)
         */
        @GetMapping
        public ResponseEntity<?> getProfile() {
                try {
                        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
                        logger.info("GET /profile - Profile request received for userId: {}", userId);
                        ProfileResponse profile = profileService.getProfile(userId);

                        logger.info("GET /profile - Profile retrieved successfully for userId: {}", userId);
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
                        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
                        logger.info("PUT /profile - Profile update request received for userId: {}", userId);
                        ProfileResponse updatedProfile = profileService.updateProfile(userId, request);

                        logger.info("PUT /profile - Profile updated successfully for userId: {}", userId);
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
         * Request OTP for signup email verification
         * Public endpoint - no token required
         */
        @PostMapping("/request-signup-otp")
        public ResponseEntity<?> requestSignupOtp(@RequestBody OtpRequest request) {
                logger.info("POST /profile/request-signup-otp - Signup OTP request for email: {}",
                                request.getEmail());
                try {
                        otpService.sendOtp(request.getEmail(), OtpType.SIGNUP);

                        logger.info("POST /profile/request-signup-otp - Signup OTP sent successfully for email: {}",
                                        request.getEmail());
                        return ResponseEntity.ok(Map.of(
                                        "success", true,
                                        "message", "Verification OTP has been sent to your email"));
                } catch (IllegalArgumentException e) {
                        logger.warn("POST /profile/request-signup-otp - Request failed for email: {}. Reason: {}",
                                        request.getEmail(), e.getMessage());
                        return ResponseEntity.badRequest().body(Map.of(
                                        "success", false,
                                        "message", e.getMessage()));
                } catch (RuntimeException e) {
                        logger.error("POST /profile/request-signup-otp - Internal error for email: {}. Error: {}",
                                        request.getEmail(), e.getMessage(), e);
                        return ResponseEntity.internalServerError().body(Map.of(
                                        "success", false,
                                        "message", "Failed to send OTP. Please try again later."));
                }
        }

        /**
         * Verify OTP for password reset without consuming it
         * Returns validation status: valid or invalid (expired treated as invalid)
         * Public endpoint - no token required
         */
        @PostMapping("/verify-otp")
        public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
                logger.info("POST /profile/verify-otp - OTP verification request for email: {}", request.getEmail());

                String status = otpService.verifyOtpWithoutConsuming(
                                request.getEmail(),
                                request.getOtp(),
                                OtpType.PASSWORD_RESET);

                logger.info("POST /profile/verify-otp - OTP verification result for email: {}, status: {}",
                                request.getEmail(), status);

                if ("valid".equals(status)) {
                        return ResponseEntity.ok(Map.of(
                                        "success", true,
                                        "status", "valid",
                                        "message", "OTP is valid"));
                } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "success", false,
                                        "status", "invalid",
                                        "message", "Invalid or expired OTP. Please request a new one."));
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
                                logger.warn("POST /profile/reset-password - Invalid OTP for email: {}",
                                                request.getEmail());
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
                        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
                        logger.info("POST /profile/change-password - Password change request received for userId: {}",
                                        userId);

                        boolean success = profileService.changePassword(userId, request.getOldPassword(),
                                        request.getNewPassword());

                        if (success) {
                                logger.info("POST /profile/change-password - Password changed successfully for userId: {}",
                                                userId);
                                return ResponseEntity.ok(Map.of(
                                                "success", true,
                                                "message", "Password changed successfully"));
                        } else {
                                logger.warn("POST /profile/change-password - Incorrect old password for userId: {}",
                                                userId);
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
