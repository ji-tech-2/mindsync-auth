package com.jitech.mindsync.controller;

import com.jitech.mindsync.service.EmailService;
import com.jitech.mindsync.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev") // biar ga public di production
public class EmailTestController {

    private static final Logger logger = LoggerFactory.getLogger(EmailTestController.class);

    @Autowired
    private EmailService emailService;
    @Autowired
    private OtpService otpService;
    
    @GetMapping("/test-email")
    public String sendTestEmail(@RequestParam String to) {
        // tujuan ini cuma buat ngesend message doang sebenarnya
        // jadi kalau OTP hard coded ga ngefek
        try {
            logger.info("Preparing to send test email to {}", to);
            // Use the OTP email as a test (sends a dummy OTP)
            emailService.sendOtpEmail(to, "123456");
            return "Test email sent successfully to " + to;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }

    @GetMapping("/test-otp")
    public String sendTestOtp(@RequestParam String email) {
        try {
            logger.info("Sending OTP to {}", email);
            otpService.sendOtp(email);
            return "OTP sent successfully to " + email;
        } catch (Exception e) {
            return "Failed to send OTP: " + e.getMessage();
        }
    }
}