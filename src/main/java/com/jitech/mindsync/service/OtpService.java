package com.jitech.mindsync.service;

import com.jitech.mindsync.model.OtpToken;
import com.jitech.mindsync.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Autowired
    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Transactional
    public void sendOtp(String email) {
        // Generate new OTP
        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);

        // Create and save OTP token
        OtpToken otpToken = new OtpToken(email, otpCode, now, expiresAt);
        otpTokenRepository.save(otpToken);

        // Send OTP via email
        emailService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<OtpToken> otpTokenOpt = otpTokenRepository
            .findByEmailAndOtpCodeAndIsUsedFalse(email, otpCode);

        if (otpTokenOpt.isEmpty()) {
            return false;
        }

        OtpToken otpToken = otpTokenOpt.get();

        // Check if OTP is expired
        if (otpToken.isExpired()) {
            return false;
        }

        // Mark as verified (but not used yet - will be used when password is changed)
        otpToken.setVerified(true);
        otpTokenRepository.save(otpToken);

        return true;
    }

    @Transactional
    public boolean validateAndUseOtp(String email, String otpCode) {
        Optional<OtpToken> otpTokenOpt = otpTokenRepository
            .findByEmailAndOtpCodeAndIsVerifiedTrueAndIsUsedFalse(email, otpCode);

        if (otpTokenOpt.isEmpty()) {
            return false;
        }

        OtpToken otpToken = otpTokenOpt.get();

        // Check if OTP is expired
        if (otpToken.isExpired()) {
            return false;
        }

        // Mark as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return true;
    }
}
