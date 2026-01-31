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
@CrossOrigin(origins = "*")
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
                "data", profile
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Update current user's profile (name, gender, occupation)
     * Requires: Bearer token in Authorization header
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody ProfileUpdateRequest request
    ) {
        try {
            String email = extractEmailFromToken(authHeader);
            ProfileResponse updatedProfile = profileService.updateProfile(email, request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "data", updatedProfile
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
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
                "message", "OTP has been sent to your email"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to send OTP. Please try again later."
            ));
        }
    }

    /**
     * Verify OTP code
     * Public endpoint - no token required
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean isValid = profileService.verifyOtp(request.getEmail(), request.getOtp());
        
        if (isValid) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP verified successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid or expired OTP"
            ));
        }
    }

    /**
     * Change password after OTP verification
     * Public endpoint - no token required
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            boolean success = profileService.changePassword(
                request.getEmail(), 
                request.getOtp(), 
                request.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid or expired OTP. Please request a new one."
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        return jwtProvider.getEmailFromToken(token);
    }
}
