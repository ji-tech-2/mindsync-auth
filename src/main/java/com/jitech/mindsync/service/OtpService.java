package com.jitech.mindsync.service;

import com.jitech.mindsync.model.OtpToken;
import com.jitech.mindsync.model.OtpType;
import com.jitech.mindsync.repository.OtpTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

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
            logger.debug("Starting new rate limit window for email: {}", email);
            rateLimitMap.put(email, new RateLimitInfo());
            return;
        }

        if (!info.incrementAndCheck()) {
            logger.warn("Rate limit exceeded for email: {}. Request count: {}", email, info.count);
            throw new RuntimeException("Too many OTP requests. Please try again later.");
        }

        logger.debug("Rate limit check passed for email: {}. Request count: {}", email, info.count);
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
    public void sendOtp(String email, OtpType otpType) {
        logger.info("Starting OTP send process for email: {}, type: {}", email, otpType);

        // Check rate limit before proceeding
        checkRateLimit(email);

        // Clean up old OTP tokens for this email and type before creating a new one
        logger.debug("Cleaning up old OTP tokens for email: {}, type: {}", email, otpType);
        otpTokenRepository.deleteByEmailAndOtpType(email, otpType);

        // Generate new OTP
        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);
        logger.debug("OTP generated for email: {}, type: {}, expires at: {}",
                email, otpType, expiresAt);

        // Create and save OTP token with hashed OTP code
        String hashedOtp = passwordEncoder.encode(otpCode);
        OtpToken otpToken = new OtpToken(email, hashedOtp, otpType, now, expiresAt);
        otpTokenRepository.save(otpToken);
        logger.debug("OTP token saved to database for email: {}, type: {}", email, otpType);

        // Send plain OTP via email (user needs the original code)
        emailService.sendOtpEmail(email, otpCode);
        logger.info("OTP process completed successfully for email: {}, type: {}", email, otpType);
    }

    @Transactional
    public boolean validateAndUseOtp(String email, String otpCode, OtpType otpType) {
        logger.info("Starting OTP validation for email: {}, type: {}", email, otpType);

        // Find unused OTP for this email and type
        Optional<OtpToken> otpTokenOpt = otpTokenRepository
                .findByEmailAndOtpTypeAndIsUsedFalse(email, otpType);

        if (otpTokenOpt.isEmpty()) {
            logger.warn("OTP validation failed - No unused OTP found for email: {}, type: {}",
                    email, otpType);
            return false;
        }

        OtpToken otpToken = otpTokenOpt.get();

        // Check if OTP is expired
        if (otpToken.isExpired()) {
            logger.warn("OTP validation failed - OTP expired for email: {}, type: {}. Expired at: {}",
                    email, otpType, otpToken.getExpiresAt());
            return false;
        }

        // Verify OTP hash matches
        if (!passwordEncoder.matches(otpCode, otpToken.getOtpCode())) {
            logger.warn("OTP validation failed - OTP mismatch for email: {}, type: {}",
                    email, otpType);
            return false;
        }

        // Mark as used to prevent reuse
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        logger.info("OTP validated and marked as used successfully for email: {}, type: {}",
                email, otpType);

        return true;
    }
}
