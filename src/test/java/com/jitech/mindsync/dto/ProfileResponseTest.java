package com.jitech.mindsync.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileResponse DTO Tests")
class ProfileResponseTest {

    @Test
    @DisplayName("Should create ProfileResponse with no-args constructor")
    void testNoArgsConstructor() {
        ProfileResponse response = new ProfileResponse();
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should create ProfileResponse with all-args constructor")
    void testAllArgsConstructor() {
        UUID userId = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1990, 1, 1);

        ProfileResponse response = new ProfileResponse(
                userId,
                "test@example.com",
                "Test User",
                dob,
                "Male",
                "Engineer",
                "Remote");

        assertEquals(userId, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals(dob, response.getDob());
        assertEquals("Male", response.getGender());
        assertEquals("Engineer", response.getOccupation());
        assertEquals("Remote", response.getWorkRmt());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testSettersAndGetters() {
        ProfileResponse response = new ProfileResponse();
        UUID userId = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1995, 5, 15);

        response.setUserId(userId);
        response.setEmail("user@test.com");
        response.setName("John Doe");
        response.setDob(dob);
        response.setGender("Female");
        response.setOccupation("Student");
        response.setWorkRmt("Hybrid");

        assertEquals(userId, response.getUserId());
        assertEquals("user@test.com", response.getEmail());
        assertEquals("John Doe", response.getName());
        assertEquals(dob, response.getDob());
        assertEquals("Female", response.getGender());
        assertEquals("Student", response.getOccupation());
        assertEquals("Hybrid", response.getWorkRmt());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void testNullValues() {
        ProfileResponse response = new ProfileResponse();
        response.setUserId(null);
        response.setEmail(null);
        response.setName(null);
        response.setDob(null);
        response.setGender(null);
        response.setOccupation(null);
        response.setWorkRmt(null);

        assertNull(response.getUserId());
        assertNull(response.getEmail());
        assertNull(response.getName());
        assertNull(response.getDob());
        assertNull(response.getGender());
        assertNull(response.getOccupation());
        assertNull(response.getWorkRmt());
    }
}
