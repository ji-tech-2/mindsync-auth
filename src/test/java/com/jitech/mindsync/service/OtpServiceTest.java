package com.jitech.mindsync.service;

import com.jitech.mindsync.model.OtpToken;
import com.jitech.mindsync.repository.OtpTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Unit Tests")
class OtpServiceTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpService otpService;

    @Nested
    @DisplayName("Generate OTP Tests")
    class GenerateOtpTests {

        @Test
        @DisplayName("Should generate 6-digit OTP")
        void generateOtp_ShouldReturn6DigitOtp() {
            // When
            String otp = otpService.generateOtp();

            // Then
            assertNotNull(otp);
            assertEquals(6, otp.length());
            assertTrue(otp.matches("\\d{6}"));
        }

        @Test
        @DisplayName("Should generate different OTPs on multiple calls")
        void generateOtp_ShouldGenerateDifferentOtps() {
            // When
            String otp1 = otpService.generateOtp();
            String otp2 = otpService.generateOtp();
            String otp3 = otpService.generateOtp();

            // Then - at least one should be different (very high probability)
            assertFalse(otp1.equals(otp2) && otp2.equals(otp3));
        }
    }

    @Nested
    @DisplayName("Send OTP Tests")
    class SendOtpTests {

        @Test
        @DisplayName("Should send OTP successfully")
        void sendOtp_WithValidEmail_ShouldSendOtp() {
            // Given
            String email = "test@example.com";
            when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

            // When
            otpService.sendOtp(email);

            // Then
            verify(otpTokenRepository, times(1)).deleteByEmail(email);
            verify(otpTokenRepository, times(1)).save(any(OtpToken.class));
            verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
        }

        @Test
        @DisplayName("Should clean up old tokens before sending new OTP")
        void sendOtp_ShouldCleanupOldTokens() {
            // Given
            String email = "test@example.com";
            when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

            // When
            otpService.sendOtp(email);

            // Then
            verify(otpTokenRepository, times(1)).deleteByEmail(email);
        }

        @Test
        @DisplayName("Should save OTP token with correct expiry time")
        void sendOtp_ShouldSaveTokenWithCorrectExpiry() {
            // Given
            String email = "test@example.com";
            when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

            ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);

            // When
            otpService.sendOtp(email);

            // Then
            verify(otpTokenRepository).save(tokenCaptor.capture());
            OtpToken savedToken = tokenCaptor.getValue();

            assertNotNull(savedToken);
            assertEquals(email, savedToken.getEmail());
            assertNotNull(savedToken.getCreatedAt());
            assertNotNull(savedToken.getExpiresAt());
            assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()));
        }

        @Test
        @DisplayName("Should hash OTP before storing")
        void sendOtp_ShouldHashOtpBeforeStoring() {
            // Given
            String email = "test@example.com";
            String hashedOtp = "hashedOtpValue";
            when(passwordEncoder.encode(anyString())).thenReturn(hashedOtp);

            ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);

            // When
            otpService.sendOtp(email);

            // Then
            verify(passwordEncoder, times(1)).encode(anyString());
            verify(otpTokenRepository).save(tokenCaptor.capture());
            assertEquals(hashedOtp, tokenCaptor.getValue().getOtpCode());
        }

        @Test
        @DisplayName("Should throw exception after rate limit exceeded")
        void sendOtp_AfterRateLimitExceeded_ShouldThrowException() {
            // Given
            String email = "test@example.com";
            when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

            // When - Send 3 OTPs (should succeed)
            otpService.sendOtp(email);
            otpService.sendOtp(email);
            otpService.sendOtp(email);

            // Then - 4th attempt should fail
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> otpService.sendOtp(email));

            assertTrue(exception.getMessage().contains("Too many OTP requests"));
        }
    }

    @Nested
    @DisplayName("Validate OTP Tests")
    class ValidateOtpTests {

        @Test
        @DisplayName("Should validate correct OTP successfully")
        void validateAndUseOtp_WithCorrectOtp_ShouldReturnTrue() {
            // Given
            String email = "test@example.com";
            String otpCode = "123456";
            String hashedOtp = "hashedOtp";

            OtpToken otpToken = new OtpToken(
                    email,
                    hashedOtp,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(10));
            otpToken.setUsed(false);

            when(otpTokenRepository.findByEmailAndIsUsedFalse(email)).thenReturn(Optional.of(otpToken));
            when(passwordEncoder.matches(otpCode, hashedOtp)).thenReturn(true);

            // When
            boolean result = otpService.validateAndUseOtp(email, otpCode);

            // Then
            assertTrue(result);
            assertTrue(otpToken.isUsed());
            verify(otpTokenRepository, times(1)).save(otpToken);
        }

        @Test
        @DisplayName("Should return false for incorrect OTP")
        void validateAndUseOtp_WithIncorrectOtp_ShouldReturnFalse() {
            // Given
            String email = "test@example.com";
            String otpCode = "123456";
            String hashedOtp = "hashedOtp";

            OtpToken otpToken = new OtpToken(
                    email,
                    hashedOtp,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(10));
            otpToken.setUsed(false);

            when(otpTokenRepository.findByEmailAndIsUsedFalse(email)).thenReturn(Optional.of(otpToken));
            when(passwordEncoder.matches(otpCode, hashedOtp)).thenReturn(false);

            // When
            boolean result = otpService.validateAndUseOtp(email, otpCode);

            // Then
            assertFalse(result);
            verify(otpTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return false when no unused OTP found")
        void validateAndUseOtp_WithNoUnusedOtp_ShouldReturnFalse() {
            // Given
            String email = "test@example.com";
            when(otpTokenRepository.findByEmailAndIsUsedFalse(email)).thenReturn(Optional.empty());

            // When
            boolean result = otpService.validateAndUseOtp(email, "123456");

            // Then
            assertFalse(result);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return false for expired OTP")
        void validateAndUseOtp_WithExpiredOtp_ShouldReturnFalse() {
            // Given
            String email = "test@example.com";
            String otpCode = "123456";

            OtpToken otpToken = new OtpToken(
                    email,
                    "hashedOtp",
                    LocalDateTime.now().minusMinutes(20),
                    LocalDateTime.now().minusMinutes(10)); // Expired 10 minutes ago
            otpToken.setUsed(false);

            when(otpTokenRepository.findByEmailAndIsUsedFalse(email)).thenReturn(Optional.of(otpToken));

            // When
            boolean result = otpService.validateAndUseOtp(email, otpCode);

            // Then
            assertFalse(result);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should mark OTP as used after successful validation")
        void validateAndUseOtp_WithValidOtp_ShouldMarkAsUsed() {
            // Given
            String email = "test@example.com";
            String otpCode = "123456";
            String hashedOtp = "hashedOtp";

            OtpToken otpToken = new OtpToken(
                    email,
                    hashedOtp,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(10));
            otpToken.setUsed(false);

            when(otpTokenRepository.findByEmailAndIsUsedFalse(email)).thenReturn(Optional.of(otpToken));
            when(passwordEncoder.matches(otpCode, hashedOtp)).thenReturn(true);

            ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);

            // When
            otpService.validateAndUseOtp(email, otpCode);

            // Then
            verify(otpTokenRepository).save(tokenCaptor.capture());
            assertTrue(tokenCaptor.getValue().isUsed());
        }
    }
}
