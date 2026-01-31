package com.jitech.mindsync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
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
        
        mailSender.send(message);
    }

    public void sendPasswordChangedEmail(String toEmail) {
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
        
        mailSender.send(message);
    }
}
