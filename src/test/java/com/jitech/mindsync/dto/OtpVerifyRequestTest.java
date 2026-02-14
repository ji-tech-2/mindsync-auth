package com.jitech.mindsync.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpVerifyRequestTest {

    @Test
    void testNoArgConstructor() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgConstructor() {
        OtpVerifyRequest request = new OtpVerifyRequest("test@example.com", "123456");
        assertEquals("test@example.com", request.getEmail());
        assertEquals("123456", request.getOtp());
    }

    @Test
    void testSetterAndGetter() {
        OtpVerifyRequest request = new OtpVerifyRequest();

        request.setEmail("user@example.com");
        request.setOtp("987654");

        assertEquals("user@example.com", request.getEmail());
        assertEquals("987654", request.getOtp());
    }

    @Test
    void testEmailSetterWithNull() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testOtpSetterWithNull() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setOtp(null);
        assertNull(request.getOtp());
    }

    @Test
    void testConstructorWithNullValues() {
        OtpVerifyRequest request = new OtpVerifyRequest(null, null);
        assertNull(request.getEmail());
        assertNull(request.getOtp());
    }
}
