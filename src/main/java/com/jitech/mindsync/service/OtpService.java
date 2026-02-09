package com.jitech.mindsync.service;

import com.jitech.mindsync.model.OtpToken;
import com.jitech.mindsync.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    // Rate limiting: max requests per email within time window
    private static final int MAX_OTP_REQUESTS = 3;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 60;
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    @Autowired
    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService,
            BCryptPasswordEncoder passwordEncoder) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    private static class RateLimitInfo {
        int count;
        LocalDateTime windowStart;

        RateLimitInfo() {
            this.count = 1;
            this.windowStart = LocalDateTime.now();
        }

        boolean isWindowExpired() {
            return LocalDateTime.now().isAfter(windowStart.plusMinutes(RATE_LIMIT_WINDOW_MINUTES));
        }

        void reset() {
            this.count = 1;
            this.windowStart = LocalDateTime.now();
        }

        boolean incrementAndCheck() {
            if (isWindowExpired()) {
                reset();
                return true;
            }
            count++;
            return count <= MAX_OTP_REQUESTS;
        }
    }

    private void checkRateLimit(String email) {
        RateLimitInfo info = rateLimitMap.get(email);

        if (info == null) {
            rateLimitMap.put(email, new RateLimitInfo());
            return;
        }

        if (!info.incrementAndCheck()) {
            throw new RuntimeException("Too many OTP requests. Please try again later.");
        }
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
        // Check rate limit before proceeding
        checkRateLimit(email);

        // Clean up old OTP tokens for this email before creating a new one
        otpTokenRepository.deleteByEmail(email);

        // Generate new OTP
        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);

        // Create and save OTP token with hashed OTP code
        String hashedOtp = passwordEncoder.encode(otpCode);
        OtpToken otpToken = new OtpToken(email, hashedOtp, now, expiresAt);
        otpTokenRepository.save(otpToken);

        // Send plain OTP via email (user needs the original code)
        emailService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    public boolean validateAndUseOtp(String email, String otpCode) {
        // Find unused OTP for this email
        Optional<OtpToken> otpTokenOpt = otpTokenRepository
                .findByEmailAndIsUsedFalse(email);

        if (otpTokenOpt.isEmpty()) {
            return false;
        }

        OtpToken otpToken = otpTokenOpt.get();

        // Check if OTP is expired
        if (otpToken.isExpired()) {
            return false;
        }

        // Verify OTP hash matches
        if (!passwordEncoder.matches(otpCode, otpToken.getOtpCode())) {
            return false;
        }

        // Mark as used to prevent reuse
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return true;
    }
}
