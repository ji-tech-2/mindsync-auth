package com.jitech.mindsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jitech.mindsync.dto.LoginRequest;
import com.jitech.mindsync.dto.RegisterRequest;
import com.jitech.mindsync.model.Genders;
import com.jitech.mindsync.model.Occupations;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.security.JwtAuthenticationFilter;
import com.jitech.mindsync.security.JwtProvider;
import com.jitech.mindsync.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // TETAP PERLU MOCKBEAN JANGAN DIHAPUS
    // KUNING-KUNING DEPRECATED DIABAIKAN SAJA
    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ObjectMapper objectMapper;
    private Users testUser;
    private Genders testGender;
    private Occupations testOccupation;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test gender
        testGender = new Genders();
        testGender.setGenderId(1);
        testGender.setGenderName("Male");

        // Setup test occupation
        testOccupation = new Occupations();
        testOccupation.setOccupationId(1);
        testOccupation.setOccupationName("Student");

        // Setup test user
        testUser = new Users();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setUsername("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
        testUser.setDob(LocalDate.of(2000, 1, 15));
        testUser.setGender(testGender);
        testUser.setOccupation(testOccupation);
    }

    @Nested
    @DisplayName("Registration Endpoint Tests")
    class RegistrationEndpointTests {

        @Test
        @DisplayName("Should register user successfully")
        void register_WithValidRequest_ShouldReturnSuccess() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("password123");
            request.setName("New User");
            request.setDob(LocalDate.of(1995, 5, 20));
            request.setGender("Male");
            request.setOccupation("Student");
            request.setWorkRmt("In-person");

            Users newUser = new Users();
            newUser.setEmail("newuser@example.com");
            newUser.setName("New User");

            when(authService.registerUser(any(RegisterRequest.class))).thenReturn(newUser);

            // When/Then
            mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Registration successful")))
                    .andExpect(jsonPath("$.data.email", is("newuser@example.com")))
                    .andExpect(jsonPath("$.data.name", is("New User")));

            verify(authService, times(1)).registerUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Should return error for duplicate email")
        void register_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");
            request.setPassword("password123");
            request.setName("Test User");
            request.setDob(LocalDate.of(1995, 5, 20));
            request.setGender("Male");

            when(authService.registerUser(any(RegisterRequest.class)))
                    .thenThrow(new IllegalArgumentException("Email already registered"));

            // When/Then
            mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Email already registered")));
        }

        @Test
        @DisplayName("Should return error for invalid gender")
        void register_WithInvalidGender_ShouldReturnBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");
            request.setName("Test User");
            request.setDob(LocalDate.of(1995, 5, 20));
            request.setGender("InvalidGender");

            when(authService.registerUser(any(RegisterRequest.class)))
                    .thenThrow(new IllegalArgumentException("Invalid gender"));

            // When/Then
            mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Invalid gender")));
        }
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ShouldReturnTokenAndUserData() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            when(authService.login("test@example.com", "password123")).thenReturn(testUser);
            when(jwtProvider.generateToken("test@example.com")).thenReturn("mock.jwt.token");

            // When/Then
            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Login successful")))
                    .andExpect(jsonPath("$.user.email", is("test@example.com")))
                    .andExpect(jsonPath("$.user.name", is("Test User")))
                    .andExpect(jsonPath("$.user.gender", is("Male")))
                    .andExpect(jsonPath("$.user.occupation", is("Student")))
                    .andExpect(cookie().exists("jwt"))
                    .andExpect(cookie().httpOnly("jwt", true));

            verify(authService, times(1)).login("test@example.com", "password123");
            verify(jwtProvider, times(1)).generateToken("test@example.com");
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("wrongPassword");

            when(authService.login("test@example.com", "wrongPassword")).thenReturn(null);

            // When/Then
            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Invalid email or password")));

            verify(jwtProvider, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Should return 401 for non-existent user")
        void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("nonexistent@example.com");
            request.setPassword("anyPassword");

            when(authService.login("nonexistent@example.com", "anyPassword")).thenReturn(null);

            // When/Then
            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Invalid email or password")));
        }

        @Test
        @DisplayName("Should handle user with null gender gracefully")
        void login_WithNullGender_ShouldReturnNullGenderInResponse() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            Users userWithNullGender = new Users();
            userWithNullGender.setUserId(UUID.randomUUID());
            userWithNullGender.setEmail("test@example.com");
            userWithNullGender.setName("Test User");
            userWithNullGender.setDob(LocalDate.of(2000, 1, 15));
            userWithNullGender.setGender(null);
            userWithNullGender.setOccupation(null);

            when(authService.login("test@example.com", "password123")).thenReturn(userWithNullGender);
            when(jwtProvider.generateToken("test@example.com")).thenReturn("mock.jwt.token");

            // When/Then
            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.user.gender").value(nullValue()))
                    .andExpect(jsonPath("$.user.occupation").value(nullValue()));
        }
    }
}
