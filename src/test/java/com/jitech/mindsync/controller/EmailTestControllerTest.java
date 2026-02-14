package com.jitech.mindsync.controller;

import com.jitech.mindsync.security.JwtProvider;
import com.jitech.mindsync.service.EmailService;
import com.jitech.mindsync.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailTestController.class)
@ActiveProfiles("dev")
@AutoConfigureMockMvc(addFilters = false)
class EmailTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void testSendTestEmailSuccess() throws Exception {
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        mockMvc.perform(get("/test-email")
                .param("to", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test email sent successfully to test@example.com"));

        verify(emailService).sendOtpEmail("test@example.com", "123456");
    }

    @Test
    void testSendTestEmailFailure() throws Exception {
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendOtpEmail(anyString(), anyString());

        mockMvc.perform(get("/test-email")
                .param("to", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Failed to send email: Email service error"));

        verify(emailService).sendOtpEmail("test@example.com", "123456");
    }

    @Test
    void testSendTestOtpSuccess() throws Exception {
        doNothing().when(otpService).sendOtp(anyString());

        mockMvc.perform(get("/test-otp")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully to test@example.com"));

        verify(otpService).sendOtp("test@example.com");
    }

    @Test
    void testSendTestOtpFailure() throws Exception {
        doThrow(new RuntimeException("OTP service error"))
                .when(otpService).sendOtp(anyString());

        mockMvc.perform(get("/test-otp")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Failed to send OTP: OTP service error"));

        verify(otpService).sendOtp("test@example.com");
    }
}
