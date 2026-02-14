package com.jitech.mindsync.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    private EmailService emailService;
    private Resend mockResend;

    @BeforeEach
    void setUp() throws ResendException {
        emailService = new EmailService();
        mockResend = mock(Resend.class);

        // Mock the emails() method to return an object with send() method
        var mockEmailsApi = mock(com.resend.services.emails.Emails.class);
        when(mockResend.emails()).thenReturn(mockEmailsApi);

        // Mock successful email sending by default
        CreateEmailResponse mockResponse = mock(CreateEmailResponse.class);
        when(mockResponse.getId()).thenReturn("test-email-id");
        when(mockEmailsApi.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        ReflectionTestUtils.setField(emailService, "resendApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "resend", mockResend);
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize with valid API key")
        void init_WithValidApiKey_ShouldInitializeSuccessfully() {
            // Given
            EmailService service = new EmailService();
            ReflectionTestUtils.setField(service, "resendApiKey", "valid-key");

            // When/Then - should not throw
            assertDoesNotThrow(() -> service.init());
        }

        @Test
        @DisplayName("Should handle missing API key gracefully")
        void init_WithMissingApiKey_ShouldNotThrow() {
            // Given
            EmailService service = new EmailService();
            ReflectionTestUtils.setField(service, "resendApiKey", "");

            // When/Then - should not throw
            assertDoesNotThrow(() -> service.init());
        }
    }

    @Nested
    @DisplayName("Send OTP Email Tests")
    class SendOtpEmailTests {

        @Test
        @DisplayName("Should send OTP email successfully")
        void sendOtpEmail_WithValidParameters_ShouldSendEmail() throws ResendException {
            // Given
            String email = "test@example.com";
            String otp = "123456";

            // When
            emailService.sendOtpEmail(email, otp);

            // Then
            var mockEmailsApi = mockResend.emails();
            ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
            verify(mockEmailsApi).send(captor.capture());

            CreateEmailOptions options = captor.getValue();
            assertTrue(options.getTo().toString().contains(email));
            assertEquals("MindSync - Password Reset OTP", options.getSubject());
            assertTrue(options.getHtml().contains(otp));
        }

        @Test
        @DisplayName("Should sanitize OTP to prevent XSS")
        void sendOtpEmail_WithMaliciousOtp_ShouldSanitize() throws ResendException {
            // Given
            String email = "test@example.com";
            String maliciousOtp = "<script>alert('xss')</script>";

            // When
            emailService.sendOtpEmail(email, maliciousOtp);

            // Then
            var mockEmailsApi = mockResend.emails();
            ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
            verify(mockEmailsApi).send(captor.capture());

            CreateEmailOptions options = captor.getValue();
            // The HTML should escape the script tags
            assertFalse(options.getHtml().contains("<script>"));
            assertTrue(options.getHtml().contains("&lt;") || options.getHtml().contains("&amp;"));
        }

        @Test
        @DisplayName("Should throw exception when email service not configured")
        void sendOtpEmail_WhenNotConfigured_ShouldThrowException() {
            // Given
            ReflectionTestUtils.setField(emailService, "resend", null);

            // When/Then
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> emailService.sendOtpEmail("test@example.com", "123456"));

            assertTrue(exception.getMessage().contains("not configured"));
        }

        @Test
        @DisplayName("Should throw RuntimeException when Resend API fails")
        void sendOtpEmail_WhenResendFails_ShouldThrowRuntimeException() throws ResendException {
            // Given
            String email = "test@example.com";
            String otp = "123456";
            var mockEmailsApi = mockResend.emails();
            when(mockEmailsApi.send(any(CreateEmailOptions.class)))
                    .thenThrow(new ResendException("API Error"));

            // When/Then
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> emailService.sendOtpEmail(email, otp));

            assertTrue(exception.getMessage().contains("Failed to send email"));
        }

        @Test
        @DisplayName("Should include OTP in email content")
        void sendOtpEmail_ShouldIncludeOtpInContent() throws ResendException {
            // Given
            String email = "test@example.com";
            String otp = "987654";

            // When
            emailService.sendOtpEmail(email, otp);

            // Then
            var mockEmailsApi = mockResend.emails();
            ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
            verify(mockEmailsApi).send(captor.capture());

            String htmlContent = captor.getValue().getHtml();
            assertTrue(htmlContent.contains(otp));
            assertTrue(htmlContent.contains("Password Reset Request"));
            assertTrue(htmlContent.contains("10 minutes"));
        }
    }

    @Nested
    @DisplayName("Send Password Changed Email Tests")
    class SendPasswordChangedEmailTests {

        @Test
        @DisplayName("Should send password changed email successfully")
        void sendPasswordChangedEmail_WithValidEmail_ShouldSendEmail() throws ResendException {
            // Given
            String email = "test@example.com";

            // When
            emailService.sendPasswordChangedEmail(email);

            // Then
            var mockEmailsApi = mockResend.emails();
            ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
            verify(mockEmailsApi).send(captor.capture());

            CreateEmailOptions options = captor.getValue();
            assertTrue(options.getTo().toString().contains(email));
            assertEquals("MindSync - Password Changed Successfully", options.getSubject());
            assertTrue(options.getHtml().contains("Password Changed Successfully"));
        }

        @Test
        @DisplayName("Should throw exception when email service not configured")
        void sendPasswordChangedEmail_WhenNotConfigured_ShouldThrowException() {
            // Given
            ReflectionTestUtils.setField(emailService, "resend", null);

            // When/Then
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> emailService.sendPasswordChangedEmail("test@example.com"));

            assertTrue(exception.getMessage().contains("not configured"));
        }

        @Test
        @DisplayName("Should not throw exception when Resend API fails")
        void sendPasswordChangedEmail_WhenResendFails_ShouldNotThrow() throws ResendException {
            // Given
            String email = "test@example.com";
            var mockEmailsApi = mockResend.emails();
            when(mockEmailsApi.send(any(CreateEmailOptions.class)))
                    .thenThrow(new ResendException("API Error"));

            // When/Then - Should NOT throw exception (password was already changed)
            assertDoesNotThrow(() -> emailService.sendPasswordChangedEmail(email));

            // Verify the send was attempted
            verify(mockEmailsApi).send(any(CreateEmailOptions.class));
        }

        @Test
        @DisplayName("Should include warning message in email content")
        void sendPasswordChangedEmail_ShouldIncludeWarning() throws ResendException {
            // Given
            String email = "test@example.com";

            // When
            emailService.sendPasswordChangedEmail(email);

            // Then
            var mockEmailsApi = mockResend.emails();
            ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
            verify(mockEmailsApi).send(captor.capture());

            String htmlContent = captor.getValue().getHtml();
            assertTrue(htmlContent.contains("Password Changed Successfully"));
            assertTrue(htmlContent.contains("contact our support"));
        }
    }
}
