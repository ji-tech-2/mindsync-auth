package com.jitech.mindsync.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpRequestTest {

    @Test
    void testNoArgConstructor() {
        OtpRequest request = new OtpRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgConstructor() {
        OtpRequest request = new OtpRequest("test@example.com");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testSetterAndGetter() {
        OtpRequest request = new OtpRequest();
        request.setEmail("user@example.com");
        assertEquals("user@example.com", request.getEmail());
    }

    @Test
    void testEmailSetterWithNull() {
        OtpRequest request = new OtpRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testConstructorWithNull() {
        OtpRequest request = new OtpRequest(null);
        assertNull(request.getEmail());
    }
}
