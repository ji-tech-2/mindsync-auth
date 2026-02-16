package com.jitech.mindsync.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordRequestTest {

    @Test
    void testNoArgConstructor() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgConstructor() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "test@example.com",
                "123456",
                "newPassword123");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("123456", request.getOtp());
        assertEquals("newPassword123", request.getNewPassword());
    }

    @Test
    void testSetterAndGetter() {
        ResetPasswordRequest request = new ResetPasswordRequest();

        request.setEmail("user@example.com");
        request.setOtp("987654");
        request.setNewPassword("securePassword456");

        assertEquals("user@example.com", request.getEmail());
        assertEquals("987654", request.getOtp());
        assertEquals("securePassword456", request.getNewPassword());
    }

    @Test
    void testEmailSetterWithNull() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testOtpSetterWithNull() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setOtp(null);
        assertNull(request.getOtp());
    }

    @Test
    void testNewPasswordSetterWithNull() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword(null);
        assertNull(request.getNewPassword());
    }
}
