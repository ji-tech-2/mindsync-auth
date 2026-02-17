package com.jitech.mindsync.service;

import com.jitech.mindsync.model.OtpType;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String FROM_EMAIL = "MindSync <noreply@mail.mindsync.my>";

    private Resend resend;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @PostConstruct
    public void init() {
        logger.info("Initializing EmailService...");
        if (resendApiKey == null || resendApiKey.isBlank()) {
            logger.warn(
                    "Resend API key is missing. Set RESEND_API environment variable to enable email functionality.");
        } else {
            resend = new Resend(resendApiKey);
            logger.info("EmailService initialized successfully with Resend API");
        }
    }

    private void validateEmailConfigured() {
        if (resend == null) {
            logger.error("Attempt to send email failed - Email service is not configured");
            throw new RuntimeException("Email service is not configured. Please contact support.");
        }
    }

    public void sendOtpEmail(String toEmail, String otp, OtpType otpType) {
        logger.info("Starting OTP email send process for: {}, type: {}", toEmail, otpType);
        validateEmailConfigured();

        // Sanitize OTP to prevent XSS attacks
        String sanitizedOtp = HtmlUtils.htmlEscape(otp);
        logger.debug("OTP sanitized, length: {}", sanitizedOtp.length());

        String htmlContent;
        String subject;

        if (otpType == OtpType.SIGNUP) {
            // Email template for signup verification
            htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
                    "<h2 style=\"color: #333;\">Welcome to MindSync!</h2>" +
                    "<p>Hello,</p>" +
                    "<p>Thank you for signing up for MindSync. To complete your registration and verify your email address, please use the verification code below:</p>"
                    +
                    "<p style=\"font-size: 24px; font-weight: bold; color: #28a745; " +
                    "background-color: #f1f8f4; padding: 15px; text-align: center; " +
                    "border-radius: 5px;\">Your Verification Code: " + sanitizedOtp + "</p>" +
                    "<p>This code will expire in <strong>10 minutes</strong>.</p>" +
                    "<p>If you did not create a MindSync account, please disregard this email.</p>" +
                    "<br>" +
                    "<p>Best regards,<br>MindSync Team</p>" +
                    "</div>";
            subject = "MindSync - Email Verification OTP";
        } else {
            // Email template for password reset
            htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
                    "<h2 style=\"color: #333;\">Password Reset Request</h2>" +
                    "<p>Hello,</p>" +
                    "<p>You have requested to reset your password for your MindSync account.</p>" +
                    "<p style=\"font-size: 24px; font-weight: bold; color: #007bff; " +
                    "background-color: #f8f9fa; padding: 15px; text-align: center; " +
                    "border-radius: 5px;\">Your Reset Code: " + sanitizedOtp + "</p>" +
                    "<p>This code will expire in <strong>10 minutes</strong>.</p>" +
                    "<p>If you did not request this, please ignore this email.</p>" +
                    "<br>" +
                    "<p>Best regards,<br>MindSync Team</p>" +
                    "</div>";
            subject = "MindSync - Password Reset OTP";
        }

        CreateEmailOptions createEmailOptions = CreateEmailOptions.builder()
                .from(FROM_EMAIL)
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();

        try {
            logger.debug("Sending OTP email via Resend API to: {}", toEmail);
            CreateEmailResponse response = resend.emails().send(createEmailOptions);
            logger.info("OTP email sent successfully to {}. Email ID: {}", toEmail, response.getId());
        } catch (ResendException e) {
            logger.error("Failed to send OTP email to {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    public void sendPasswordChangedEmail(String toEmail) {
        logger.info("Starting password changed confirmation email send process for: {}", toEmail);
        validateEmailConfigured();

        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
                "<h2 style=\"color: #333;\">Password Changed Successfully</h2>" +
                "<p>Hello,</p>" +
                "<p>Your password has been changed successfully.</p>" +
                "<p style=\"color: #dc3545;\"><strong>If you did not make this change, " +
                "please contact our support immediately.</strong></p>" +
                "<br>" +
                "<p>Best regards,<br>MindSync Team</p>" +
                "</div>";

        CreateEmailOptions createEmailOptions = CreateEmailOptions.builder()
                .from(FROM_EMAIL)
                .to(toEmail)
                .subject("MindSync - Password Changed Successfully")
                .html(htmlContent)
                .build();

        try {
            logger.debug("Sending password changed confirmation email via Resend API to: {}", toEmail);
            CreateEmailResponse response = resend.emails().send(createEmailOptions);
            logger.info("Password changed email sent successfully to {}. Email ID: {}", toEmail, response.getId());
        } catch (ResendException e) {
            logger.error("Failed to send password changed confirmation email to {}. Error: {}", toEmail, e.getMessage(),
                    e);
            // Don't throw exception for confirmation emails - password was already changed
            // Just log the error and continue
        }
    }
}
