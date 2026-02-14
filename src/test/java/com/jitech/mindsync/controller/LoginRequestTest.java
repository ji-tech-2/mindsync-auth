package com.jitech.mindsync.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {

    @Test
    @DisplayName("Should set and get email correctly")
    void testSetAndGetEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    @DisplayName("Should set and get password correctly")
    void testSetAndGetPassword() {
        LoginRequest request = new LoginRequest();
        request.setPassword("password123");
        assertEquals("password123", request.getPassword());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        request.setPassword(null);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }
}
