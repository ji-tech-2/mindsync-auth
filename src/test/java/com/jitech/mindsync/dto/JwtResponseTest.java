package com.jitech.mindsync.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtResponseTest {

    @Test
    void testConstructor() {
        JwtResponse response = new JwtResponse("jwt-token-123", "test@example.com");

        assertEquals("jwt-token-123", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Bearer", response.getType()); // Default value
    }

    @Test
    void testTokenSetterAndGetter() {
        JwtResponse response = new JwtResponse("initial-token", "user@example.com");

        response.setToken("new-token-456");
        assertEquals("new-token-456", response.getToken());
    }

    @Test
    void testEmailSetterAndGetter() {
        JwtResponse response = new JwtResponse("token", "initial@example.com");

        response.setEmail("updated@example.com");
        assertEquals("updated@example.com", response.getEmail());
    }

    @Test
    void testTypeSetterAndGetter() {
        JwtResponse response = new JwtResponse("token", "user@example.com");

        // Test default value
        assertEquals("Bearer", response.getType());

        // Test setter
        response.setType("Custom");
        assertEquals("Custom", response.getType());
    }

    @Test
    void testConstructorWithNullValues() {
        JwtResponse response = new JwtResponse(null, null);

        assertNull(response.getToken());
        assertNull(response.getEmail());
        assertEquals("Bearer", response.getType()); // Default value should still be set
    }

    @Test
    void testSetTokenToNull() {
        JwtResponse response = new JwtResponse("token", "user@example.com");
        response.setToken(null);
        assertNull(response.getToken());
    }

    @Test
    void testSetEmailToNull() {
        JwtResponse response = new JwtResponse("token", "user@example.com");
        response.setEmail(null);
        assertNull(response.getEmail());
    }

    @Test
    void testSetTypeToNull() {
        JwtResponse response = new JwtResponse("token", "user@example.com");
        response.setType(null);
        assertNull(response.getType());
    }
}
