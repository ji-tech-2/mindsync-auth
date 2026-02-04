package com.jitech.mindsync.service;

import com.jitech.mindsync.dto.RegisterRequest;
import com.jitech.mindsync.model.Genders;
import com.jitech.mindsync.model.Occupations;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.model.WorkRemotes;
import com.jitech.mindsync.repository.GendersRepository;
import com.jitech.mindsync.repository.OccupationsRepository;
import com.jitech.mindsync.repository.UserRepository;
import com.jitech.mindsync.repository.WorkRemotesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GendersRepository gendersRepository;

    @Mock
    private OccupationsRepository occupationsRepository;

    @Mock
    private WorkRemotesRepository workRemotesRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private Genders testGender;
    private Occupations testOccupation;
    private WorkRemotes testWorkRemote;
    private Users testUser;

    @BeforeEach
    void setUp() {
        // Setup test gender
        testGender = new Genders();
        testGender.setGenderId(1);
        testGender.setGenderName("Male");

        // Setup test occupation
        testOccupation = new Occupations();
        testOccupation.setOccupationId(1);
        testOccupation.setOccupationName("Student");

        // Setup test work remote
        testWorkRemote = new WorkRemotes();
        testWorkRemote.setWorkRmtId(1);
        testWorkRemote.setWorkRmtName("In-person");

        // Setup valid register request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setName("Test User");
        validRegisterRequest.setDob(LocalDate.of(2000, 1, 15));
        validRegisterRequest.setGender("Male");
        validRegisterRequest.setOccupation("Student");
        validRegisterRequest.setWorkRmt("In-person");

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
        testUser.setWorkRmt(testWorkRemote);
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void registerUser_WithValidData_ShouldSucceed() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(gendersRepository.findByGenderName("Male")).thenReturn(Optional.of(testGender));
            when(occupationsRepository.findByOccupationName("Student")).thenReturn(Optional.of(testOccupation));
            when(workRemotesRepository.findByWorkRmtName("In-person")).thenReturn(Optional.of(testWorkRemote));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            Users result = authService.registerUser(validRegisterRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getName()).isEqualTo("Test User");
            verify(userRepository, times(1)).save(any(Users.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerUser_WithExistingEmail_ShouldThrowException() {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).save(any(Users.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid gender")
        void registerUser_WithInvalidGender_ShouldThrowException() {
            // Given
            validRegisterRequest.setGender("InvalidGender");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(gendersRepository.findByGenderName("InvalidGender")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid gender");

            verify(userRepository, never()).save(any(Users.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid occupation")
        void registerUser_WithInvalidOccupation_ShouldThrowException() {
            // Given
            validRegisterRequest.setOccupation("InvalidOccupation");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(gendersRepository.findByGenderName("Male")).thenReturn(Optional.of(testGender));
            when(occupationsRepository.findByOccupationName("InvalidOccupation")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid occupation");

            verify(userRepository, never()).save(any(Users.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid work remote status")
        void registerUser_WithInvalidWorkRemote_ShouldThrowException() {
            // Given
            validRegisterRequest.setWorkRmt("InvalidWorkRemote");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(gendersRepository.findByGenderName("Male")).thenReturn(Optional.of(testGender));
            when(occupationsRepository.findByOccupationName("Student")).thenReturn(Optional.of(testOccupation));
            when(workRemotesRepository.findByWorkRmtName("InvalidWorkRemote")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid work remote status");

            verify(userRepository, never()).save(any(Users.class));
        }

        @Test
        @DisplayName("Should use default occupation when null")
        void registerUser_WithNullOccupation_ShouldUseDefault() {
            // Given
            validRegisterRequest.setOccupation(null);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(gendersRepository.findByGenderName("Male")).thenReturn(Optional.of(testGender));
            when(occupationsRepository.findByOccupationName("Student")).thenReturn(Optional.of(testOccupation));
            when(workRemotesRepository.findByWorkRmtName("In-person")).thenReturn(Optional.of(testWorkRemote));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            Users result = authService.registerUser(validRegisterRequest);

            // Then
            assertThat(result).isNotNull();
            verify(occupationsRepository).findByOccupationName("Student");
        }

        @Test
        @DisplayName("Should use default work remote when null")
        void registerUser_WithNullWorkRemote_ShouldUseDefault() {
            // Given
            validRegisterRequest.setWorkRmt(null);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(gendersRepository.findByGenderName("Male")).thenReturn(Optional.of(testGender));
            when(occupationsRepository.findByOccupationName("Student")).thenReturn(Optional.of(testOccupation));
            when(workRemotesRepository.findByWorkRmtName("In-person")).thenReturn(Optional.of(testWorkRemote));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            Users result = authService.registerUser(validRegisterRequest);

            // Then
            assertThat(result).isNotNull();
            verify(workRemotesRepository).findByWorkRmtName("In-person");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ShouldReturnUser() {
            // Given
            String email = "test@example.com";
            String rawPassword = "password123";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(true);

            // When
            Users result = authService.login(email, rawPassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should return null for non-existent email")
        void login_WithNonExistentEmail_ShouldReturnNull() {
            // Given
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When
            Users result = authService.login(email, "anyPassword");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for wrong password")
        void login_WithWrongPassword_ShouldReturnNull() {
            // Given
            String email = "test@example.com";
            String wrongPassword = "wrongPassword";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(wrongPassword, testUser.getPassword())).thenReturn(false);

            // When
            Users result = authService.login(email, wrongPassword);

            // Then
            assertThat(result).isNull();
        }
    }
}
