package com.jitech.mindsync.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void validateConfiguration() {
        if (fromEmail == null || fromEmail.isBlank()) {
            logger.warn("Email configuration is missing. Set MAIL_USERNAME and MAIL_PASSWORD environment variables to enable email functionality.");
        }
    }

    private void validateEmailConfigured() {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("Email service is not configured. Please contact support.");
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        validateEmailConfigured();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("MindSync - Password Reset OTP");
        message.setText(
            "Hello,\n\n" +
            "You have requested to reset your password for your MindSync account.\n\n" +
            "Your OTP verification code is: " + otp + "\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "MindSync Team"
        );
        
        try {
            mailSender.send(message);
        } catch (MailException e) {
            logger.error("Failed to send OTP email to {}", toEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    public void sendPasswordChangedEmail(String toEmail) {
        validateEmailConfigured();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("MindSync - Password Changed Successfully");
        message.setText(
            "Hello,\n\n" +
            "Your password has been changed successfully.\n\n" +
            "If you did not make this change, please contact our support immediately.\n\n" +
            "Best regards,\n" +
            "MindSync Team"
        );
        
        try {
            mailSender.send(message);
        } catch (MailException e) {
            logger.error("Failed to send password changed confirmation email to {}", toEmail, e);
            // Don't throw exception for confirmation emails - password was already changed
            // Just log the error and continue
        }
    }
}
