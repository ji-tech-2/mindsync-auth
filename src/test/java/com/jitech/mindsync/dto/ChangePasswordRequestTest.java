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
                "oldPassword123",
                "newPassword123");

        assertEquals("oldPassword123", request.getOldPassword());
        assertEquals("newPassword123", request.getNewPassword());
    }

    @Test
    void testSetterAndGetter() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        request.setOldPassword("currentPassword456");
        request.setNewPassword("securePassword456");

        assertEquals("currentPassword456", request.getOldPassword());
        assertEquals("securePassword456", request.getNewPassword());
    }

    @Test
    void testOldPasswordSetterWithNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(null);
        assertNull(request.getOldPassword());
    }

    @Test
    void testNewPasswordSetterWithNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword(null);
        assertNull(request.getNewPassword());
    }
}
