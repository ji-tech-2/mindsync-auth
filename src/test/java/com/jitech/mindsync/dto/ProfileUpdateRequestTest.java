package com.jitech.mindsync.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileUpdateRequest DTO Tests")
class ProfileUpdateRequestTest {

    @Test
    @DisplayName("Should create ProfileUpdateRequest with no-args constructor")
    void testNoArgsConstructor() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        assertNotNull(request);
    }

    @Test
    @DisplayName("Should create ProfileUpdateRequest with all-args constructor")
    void testAllArgsConstructor() {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "John Doe",
                dob,
                "Male",
                "Engineer",
                "Remote");

        assertEquals("John Doe", request.getName());
        assertEquals(dob, request.getDob());
        assertEquals("Male", request.getGender());
        assertEquals("Engineer", request.getOccupation());
        assertEquals("Remote", request.getWorkRmt());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testSettersAndGetters() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        LocalDate dob = LocalDate.of(1995, 5, 15);

        request.setName("Jane Smith");
        request.setDob(dob);
        request.setGender("Female");
        request.setOccupation("Student");
        request.setWorkRmt("Hybrid");

        assertEquals("Jane Smith", request.getName());
        assertEquals(dob, request.getDob());
        assertEquals("Female", request.getGender());
        assertEquals("Student", request.getOccupation());
        assertEquals("Hybrid", request.getWorkRmt());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void testNullValues() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName(null);
        request.setDob(null);
        request.setGender(null);
        request.setOccupation(null);
        request.setWorkRmt(null);

        assertNull(request.getName());
        assertNull(request.getDob());
        assertNull(request.getGender());
        assertNull(request.getOccupation());
        assertNull(request.getWorkRmt());
    }

    @Test
    @DisplayName("Should handle empty strings correctly")
    void testEmptyStrings() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "",
                null,
                "",
                "",
                "");

        assertEquals("", request.getName());
        assertNull(request.getDob());
        assertEquals("", request.getGender());
        assertEquals("", request.getOccupation());
        assertEquals("", request.getWorkRmt());
    }
}
