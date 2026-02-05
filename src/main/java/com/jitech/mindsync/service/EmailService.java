package com.jitech.mindsync.service;

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
        if (resendApiKey == null || resendApiKey.isBlank()) {
            logger.warn("Resend API key is missing. Set RESEND_API environment variable to enable email functionality.");
        } else {
            resend = new Resend(resendApiKey);
        }
    }

    private void validateEmailConfigured() {
        if (resend == null) {
            throw new RuntimeException("Email service is not configured. Please contact support.");
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        validateEmailConfigured();
        
        // Sanitize OTP to prevent XSS attacks
        String sanitizedOtp = HtmlUtils.htmlEscape(otp);
        
        String htmlContent = 
            "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
            "<h2 style=\"color: #333;\">Password Reset Request</h2>" +
            "<p>Hello,</p>" +
            "<p>You have requested to reset your password for your MindSync account.</p>" +
            "<p style=\"font-size: 24px; font-weight: bold; color: #007bff; " +
            "background-color: #f8f9fa; padding: 15px; text-align: center; " +
            "border-radius: 5px;\">Your OTP: " + sanitizedOtp + "</p>" +
            "<p>This code will expire in <strong>10 minutes</strong>.</p>" +
            "<p>If you did not request this, please ignore this email.</p>" +
            "<br>" +
            "<p>Best regards,<br>MindSync Team</p>" +
            "</div>";

        CreateEmailOptions createEmailOptions = CreateEmailOptions.builder()
                .from(FROM_EMAIL)
                .to(toEmail)
                .subject("MindSync - Password Reset OTP")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(createEmailOptions);
            logger.info("OTP email sent successfully to {}. Email ID: {}", toEmail, response.getId());
        } catch (ResendException e) {
            logger.error("Failed to send OTP email to {}", toEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    public void sendPasswordChangedEmail(String toEmail) {
        validateEmailConfigured();
        
        String htmlContent = 
            "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
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
            CreateEmailResponse response = resend.emails().send(createEmailOptions);
            logger.info("Password changed email sent successfully to {}. Email ID: {}", toEmail, response.getId());
        } catch (ResendException e) {
            logger.error("Failed to send password changed confirmation email to {}", toEmail, e);
            // Don't throw exception for confirmation emails - password was already changed
            // Just log the error and continue
        }
    }
}
