package com.jitech.mindsync.controller;

import com.jitech.mindsync.service.EmailService;
import com.jitech.mindsync.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailTestController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private OtpService otpService;
    
    @GetMapping("/test-email")
    public String sendTestEmail(@RequestParam String to) {
        try {
            System.out.println("Preparing to send test email to " + to);
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
            System.out.println("Sending OTP to " + email);
            otpService.sendOtp(email);
            return "OTP sent successfully to " + email + "\n\nVerify with: http://localhost:8080/test-verify-otp?email=" + email;
        } catch (Exception e) {
            return "Failed to send OTP: " + e.getMessage();
        }
    }

    @GetMapping("/test-verify-otp")
    public String verifyTestOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            System.out.println("Verifying OTP: " + otp + " for email: " + email);
            String result = otpService.verifyOtp(email, otp);
            if ("success".equals(result)) {
                return "✓ OTP verified successfully for " + email;
            } else {
                return "✗ Invalid or expired OTP for " + email + 
                       "\n\nPossible reasons:" +
                       "\n- OTP has expired (5 minutes)" +
                       "\n- OTP already used" +
                       "\n- Wrong OTP code" +
                       "\n- Email doesn't match";
            }
        } catch (Exception e) {
            return "Failed to verify OTP: " + e.getMessage();
        }
    }
}