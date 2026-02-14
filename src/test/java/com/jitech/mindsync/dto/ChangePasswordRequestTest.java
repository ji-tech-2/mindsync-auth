package com.jitech.mindsync.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordRequestTest {

    @Test
    void testNoArgConstructor() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgConstructor() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "test@example.com",
                "123456",
                "newPassword123");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("123456", request.getOtp());
        assertEquals("newPassword123", request.getNewPassword());
    }

    @Test
    void testSetterAndGetter() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        request.setEmail("user@example.com");
        request.setOtp("987654");
        request.setNewPassword("securePassword456");

        assertEquals("user@example.com", request.getEmail());
        assertEquals("987654", request.getOtp());
        assertEquals("securePassword456", request.getNewPassword());
    }

    @Test
    void testEmailSetterWithNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testOtpSetterWithNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOtp(null);
        assertNull(request.getOtp());
    }

    @Test
    void testNewPasswordSetterWithNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword(null);
        assertNull(request.getNewPassword());
    }
}
