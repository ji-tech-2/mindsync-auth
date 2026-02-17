package com.jitech.mindsync.service;

import com.jitech.mindsync.dto.ProfileResponse;
import com.jitech.mindsync.dto.ProfileUpdateRequest;
import com.jitech.mindsync.model.Genders;
import com.jitech.mindsync.model.Occupations;
import com.jitech.mindsync.model.OtpType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Unit Tests")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GendersRepository gendersRepository;

    @Mock
    private OccupationsRepository occupationsRepository;

    @Mock
    private WorkRemotesRepository workRemotesRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private Users testUser;
    private Genders testGender;
    private Occupations testOccupation;
    private WorkRemotes testWorkRemote;

    @BeforeEach
    void setUp() {
        testGender = new Genders();
        testGender.setGenderId(1);
        testGender.setGenderName("Male");

        testOccupation = new Occupations();
        testOccupation.setOccupationId(1);
        testOccupation.setOccupationName("Student");

        testWorkRemote = new WorkRemotes();
        testWorkRemote.setWorkRmtId(1);
        testWorkRemote.setWorkRmtName("Remote");

        testUser = new Users();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setDob(LocalDate.of(2000, 1, 1));
        testUser.setGender(testGender);
        testUser.setOccupation(testOccupation);
        testUser.setWorkRmt(testWorkRemote);
        testUser.setPassword("hashedPassword");
    }

    @Nested
    @DisplayName("Get Profile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should return profile for valid userId")
        void getProfile_WithValidUserId_ShouldReturnProfile() {
            // Given
            String userId = testUser.getUserId().toString();
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

            // When
            ProfileResponse result = profileService.getProfile(userId);

            // Then
            assertNotNull(result);
            assertEquals(testUser.getUserId(), result.getUserId());
            assertEquals("test@example.com", result.getEmail());
            assertEquals("Test User", result.getName());
            assertEquals(LocalDate.of(2000, 1, 1), result.getDob());
            assertEquals("Male", result.getGender());
            assertEquals("Student", result.getOccupation());
            assertEquals("Remote", result.getWorkRmt());

            verify(userRepository, times(1)).findById(testUser.getUserId());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getProfile_WithInvalidUserId_ShouldThrowException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.getProfile(nonExistentId.toString()));

            assertEquals("User not found", exception.getMessage());
            verify(userRepository, times(1)).findById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle user with null gender, occupation, and work remote")
        void getProfile_WithNullFields_ShouldReturnProfileWithNulls() {
            // Given
            testUser.setGender(null);
            testUser.setOccupation(null);
            testUser.setWorkRmt(null);
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

            // When
            ProfileResponse result = profileService.getProfile(testUser.getUserId().toString());

            // Then
            assertNotNull(result);
            assertNull(result.getGender());
            assertNull(result.getOccupation());
            assertNull(result.getWorkRmt());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update all fields successfully")
        void updateProfile_WithAllFields_ShouldUpdateSuccessfully() {
            // Given
            String userId = testUser.getUserId().toString();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setName("Updated Name");
            request.setGender("Female");
            request.setOccupation("Engineer");
            request.setWorkRmt("Hybrid");

            Genders newGender = new Genders();
            newGender.setGenderId(2);
            newGender.setGenderName("Female");

            Occupations newOccupation = new Occupations();
            newOccupation.setOccupationId(2);
            newOccupation.setOccupationName("Engineer");

            WorkRemotes newWorkRemote = new WorkRemotes();
            newWorkRemote.setWorkRmtId(2);
            newWorkRemote.setWorkRmtName("Hybrid");

            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(gendersRepository.findByGenderName("Female")).thenReturn(Optional.of(newGender));
            when(occupationsRepository.findByOccupationName("Engineer")).thenReturn(Optional.of(newOccupation));
            when(workRemotesRepository.findByWorkRmtName("Hybrid")).thenReturn(Optional.of(newWorkRemote));
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            ProfileResponse result = profileService.updateProfile(userId, request);

            // Then
            assertNotNull(result);
            verify(userRepository, times(1)).save(testUser);
            assertEquals("Updated Name", testUser.getName());
        }

        @Test
        @DisplayName("Should update only name")
        void updateProfile_WithOnlyName_ShouldUpdateName() {
            // Given
            String userId = testUser.getUserId().toString();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setName("New Name");

            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            ProfileResponse result = profileService.updateProfile(userId, request);

            // Then
            assertNotNull(result);
            assertEquals("New Name", testUser.getName());
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void updateProfile_WithInvalidUserId_ShouldThrowException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setName("New Name");
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.updateProfile(nonExistentId.toString(), request));

            assertEquals("User not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid gender")
        void updateProfile_WithInvalidGender_ShouldThrowException() {
            // Given
            String userId = testUser.getUserId().toString();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setGender("InvalidGender");

            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(gendersRepository.findByGenderName("InvalidGender")).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.updateProfile(userId, request));

            assertTrue(exception.getMessage().contains("Invalid gender"));
        }

        @Test
        @DisplayName("Should throw exception for invalid occupation")
        void updateProfile_WithInvalidOccupation_ShouldThrowException() {
            // Given
            String userId = testUser.getUserId().toString();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setOccupation("InvalidOccupation");

            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(occupationsRepository.findByOccupationName("InvalidOccupation")).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.updateProfile(userId, request));

            assertTrue(exception.getMessage().contains("Invalid occupation"));
        }

        @Test
        @DisplayName("Should throw exception for invalid work remote status")
        void updateProfile_WithInvalidWorkRemote_ShouldThrowException() {
            // Given
            String userId = testUser.getUserId().toString();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setWorkRmt("InvalidWorkRemote");

            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(workRemotesRepository.findByWorkRmtName("InvalidWorkRemote")).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.updateProfile(userId, request));

            assertTrue(exception.getMessage().contains("Invalid work remote status"));
        }

        @Test
        @DisplayName("Should trim whitespace from fields")
        void updateProfile_WithWhitespace_ShouldTrimFields() {
            // Given
            String userId = testUser.getUserId().toString();
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setName("  New Name  ");

            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            profileService.updateProfile(userId, request);

            // Then
            assertEquals("New Name", testUser.getName());
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should send OTP for existing user")
        void requestPasswordReset_WithValidEmail_ShouldSendOtp() {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When
            profileService.requestPasswordReset("test@example.com");

            // Then
            verify(otpService, times(1)).sendOtp("test@example.com", OtpType.PASSWORD_RESET);
        }

        @Test
        @DisplayName("Should not throw exception for non-existent email")
        void requestPasswordReset_WithInvalidEmail_ShouldNotThrow() {
            // Given
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // When/Then - should not throw to prevent user enumeration
            assertDoesNotThrow(() -> profileService.requestPasswordReset("nonexistent@example.com"));
            verify(otpService, never()).sendOtp(anyString(), any(OtpType.class));
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password with valid OTP")
        void resetPassword_WithValidOtp_ShouldResetPassword() {
            // Given
            when(otpService.validateAndUseOtp("test@example.com", "123456", OtpType.PASSWORD_RESET))
                    .thenReturn(true);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            boolean result = profileService.resetPassword("test@example.com", "123456", "newPassword123");

            // Then
            assertTrue(result);
            verify(otpService, times(1)).validateAndUseOtp("test@example.com", "123456", OtpType.PASSWORD_RESET);
            verify(userRepository, times(1)).save(testUser);
            verify(emailService, times(1)).sendPasswordChangedEmail("test@example.com");
        }

        @Test
        @DisplayName("Should return false with invalid OTP")
        void resetPassword_WithInvalidOtp_ShouldReturnFalse() {
            // Given
            when(otpService.validateAndUseOtp("test@example.com", "wrong", OtpType.PASSWORD_RESET))
                    .thenReturn(false);

            // When
            boolean result = profileService.resetPassword("test@example.com", "wrong", "newPassword123");

            // Then
            assertFalse(result);
            verify(userRepository, never()).save(any(Users.class));
            verify(emailService, never()).sendPasswordChangedEmail(anyString());
        }

        @Test
        @DisplayName("Should throw exception for short password")
        void resetPassword_WithShortPassword_ShouldThrowException() {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.resetPassword("test@example.com", "123456", "short"));

            assertEquals("Password must be at least 8 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null password")
        void resetPassword_WithNullPassword_ShouldThrowException() {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.resetPassword("test@example.com", "123456", null));

            assertEquals("Password must be at least 8 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void resetPassword_WithNonExistentUser_ShouldThrowException() {
            // Given
            when(otpService.validateAndUseOtp("nonexistent@example.com", "123456", OtpType.PASSWORD_RESET))
                    .thenReturn(true);
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.resetPassword("nonexistent@example.com", "123456", "newPassword123"));

            assertEquals("User not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password with valid old password")
        void changePassword_WithValidOldPassword_ShouldChangePassword() {
            // Given
            String userId = testUser.getUserId().toString();
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword123", "hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");
            when(userRepository.save(any(Users.class))).thenReturn(testUser);

            // When
            boolean result = profileService.changePassword(userId, "oldPassword123", "newPassword123");

            // Then
            assertTrue(result);
            verify(passwordEncoder, times(1)).matches("oldPassword123", "hashedPassword");
            verify(userRepository, times(1)).save(testUser);
            verify(emailService, times(1)).sendPasswordChangedEmail("test@example.com");
        }

        @Test
        @DisplayName("Should return false with incorrect old password")
        void changePassword_WithIncorrectOldPassword_ShouldReturnFalse() {
            // Given
            String userId = testUser.getUserId().toString();
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            // When
            boolean result = profileService.changePassword(userId, "wrongPassword", "newPassword123");

            // Then
            assertFalse(result);
            verify(userRepository, never()).save(any(Users.class));
            verify(emailService, never()).sendPasswordChangedEmail(anyString());
        }

        @Test
        @DisplayName("Should throw exception for short password")
        void changePassword_WithShortPassword_ShouldThrowException() {
            // Given
            String userId = testUser.getUserId().toString();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.changePassword(userId, "oldPassword123", "short"));

            assertEquals("Password must be at least 8 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null password")
        void changePassword_WithNullPassword_ShouldThrowException() {
            // Given
            String userId = testUser.getUserId().toString();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.changePassword(userId, "oldPassword123", null));

            assertEquals("Password must be at least 8 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void changePassword_WithNonExistentUser_ShouldThrowException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.changePassword(nonExistentId.toString(), "oldPassword123", "newPassword123"));

            assertEquals("User not found", exception.getMessage());
        }
    }
}
