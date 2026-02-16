package com.jitech.mindsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jitech.mindsync.dto.*;
import com.jitech.mindsync.model.OtpType;
import com.jitech.mindsync.security.JwtAuthenticationFilter;
import com.jitech.mindsync.security.JwtProvider;
import com.jitech.mindsync.service.OtpService;
import com.jitech.mindsync.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProfileService profileService;

        @MockBean
        private OtpService otpService;

        @MockBean
        private JwtProvider jwtProvider;

        private ProfileResponse sampleProfile;
        private ProfileUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
                sampleProfile = new ProfileResponse();
                sampleProfile.setEmail("test@example.com");
                sampleProfile.setName("John Doe");
                sampleProfile.setGender("MALE");
                sampleProfile.setOccupation("SOFTWARE_ENGINEER");

                updateRequest = new ProfileUpdateRequest();
                updateRequest.setName("Jane Smith");
                updateRequest.setGender("FEMALE");
                updateRequest.setOccupation("DATA_SCIENTIST");
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void getProfile_Success() throws Exception {
                when(profileService.getProfile("test@example.com")).thenReturn(sampleProfile);

                mockMvc.perform(get("/profile")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                                .andExpect(jsonPath("$.data.name").value("John Doe"))
                                .andExpect(jsonPath("$.data.gender").value("MALE"))
                                .andExpect(jsonPath("$.data.occupation").value("SOFTWARE_ENGINEER"));

                verify(profileService).getProfile("test@example.com");
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void getProfile_UserNotFound() throws Exception {
                when(profileService.getProfile("test@example.com"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                mockMvc.perform(get("/profile")
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User not found"));

                verify(profileService).getProfile("test@example.com");
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void updateProfile_Success() throws Exception {
                ProfileResponse updatedProfile = new ProfileResponse();
                updatedProfile.setEmail("test@example.com");
                updatedProfile.setName("Jane Smith");
                updatedProfile.setGender("FEMALE");
                updatedProfile.setOccupation("DATA_SCIENTIST");

                when(profileService.updateProfile(eq("test@example.com"), any(ProfileUpdateRequest.class)))
                                .thenReturn(updatedProfile);

                mockMvc.perform(put("/profile")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                                .andExpect(jsonPath("$.data.name").value("Jane Smith"))
                                .andExpect(jsonPath("$.data.gender").value("FEMALE"))
                                .andExpect(jsonPath("$.data.occupation").value("DATA_SCIENTIST"));

                verify(profileService).updateProfile(eq("test@example.com"), any(ProfileUpdateRequest.class));
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void updateProfile_InvalidData() throws Exception {
                when(profileService.updateProfile(eq("test@example.com"), any(ProfileUpdateRequest.class)))
                                .thenThrow(new IllegalArgumentException("Invalid gender value"));

                mockMvc.perform(put("/profile")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid gender value"));

                verify(profileService).updateProfile(eq("test@example.com"), any(ProfileUpdateRequest.class));
        }

        @Test
        void requestOtp_Success() throws Exception {
                OtpRequest otpRequest = new OtpRequest();
                otpRequest.setEmail("test@example.com");

                doNothing().when(profileService).requestPasswordReset("test@example.com");

                mockMvc.perform(post("/profile/request-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("OTP has been sent to your email"));

                verify(profileService).requestPasswordReset("test@example.com");
        }

        @Test
        void requestOtp_UserNotFound() throws Exception {
                OtpRequest otpRequest = new OtpRequest();
                otpRequest.setEmail("nonexistent@example.com");

                doThrow(new IllegalArgumentException("User not found"))
                                .when(profileService).requestPasswordReset("nonexistent@example.com");

                mockMvc.perform(post("/profile/request-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User not found"));

                verify(profileService).requestPasswordReset("nonexistent@example.com");
        }

        @Test
        void requestOtp_EmailSendingFailure() throws Exception {
                OtpRequest otpRequest = new OtpRequest();
                otpRequest.setEmail("test@example.com");

                doThrow(new RuntimeException("Email service unavailable"))
                                .when(profileService).requestPasswordReset("test@example.com");

                mockMvc.perform(post("/profile/request-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Failed to send OTP. Please try again later."));

                verify(profileService).requestPasswordReset("test@example.com");
        }

        @Test
        void requestSignupOtp_Success() throws Exception {
                OtpRequest otpRequest = new OtpRequest();
                otpRequest.setEmail("newuser@example.com");

                doNothing().when(otpService).sendOtp("newuser@example.com", OtpType.SIGNUP);

                mockMvc.perform(post("/profile/request-signup-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Verification OTP has been sent to your email"));

                verify(otpService).sendOtp("newuser@example.com", OtpType.SIGNUP);
        }

        @Test
        void requestSignupOtp_InvalidEmail() throws Exception {
                OtpRequest otpRequest = new OtpRequest();
                otpRequest.setEmail("invalid@example.com");

                doThrow(new IllegalArgumentException("Invalid email"))
                                .when(otpService).sendOtp("invalid@example.com", OtpType.SIGNUP);

                mockMvc.perform(post("/profile/request-signup-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid email"));

                verify(otpService).sendOtp("invalid@example.com", OtpType.SIGNUP);
        }

        @Test
        void requestSignupOtp_EmailSendingFailure() throws Exception {
                OtpRequest otpRequest = new OtpRequest();
                otpRequest.setEmail("newuser@example.com");

                doThrow(new RuntimeException("Email service unavailable"))
                                .when(otpService).sendOtp("newuser@example.com", OtpType.SIGNUP);

                mockMvc.perform(post("/profile/request-signup-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Failed to send OTP. Please try again later."));

                verify(otpService).sendOtp("newuser@example.com", OtpType.SIGNUP);
        }

        @Test
        void resetPassword_Success() throws Exception {
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
                resetPasswordRequest.setEmail("test@example.com");
                resetPasswordRequest.setOtp("123456");
                resetPasswordRequest.setNewPassword("newPassword123!");

                when(profileService.resetPassword("test@example.com", "123456", "newPassword123!"))
                                .thenReturn(true);

                mockMvc.perform(post("/profile/reset-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Password reset successfully"));

                verify(profileService).resetPassword("test@example.com", "123456", "newPassword123!");
        }

        @Test
        void resetPassword_InvalidOtp() throws Exception {
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
                resetPasswordRequest.setEmail("test@example.com");
                resetPasswordRequest.setOtp("999999");
                resetPasswordRequest.setNewPassword("newPassword123!");

                when(profileService.resetPassword("test@example.com", "999999", "newPassword123!"))
                                .thenReturn(false);

                mockMvc.perform(post("/profile/reset-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Invalid or expired OTP. Please request a new one."));

                verify(profileService).resetPassword("test@example.com", "999999", "newPassword123!");
        }

        @Test
        void resetPassword_UserNotFound() throws Exception {
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
                resetPasswordRequest.setEmail("nonexistent@example.com");
                resetPasswordRequest.setOtp("123456");
                resetPasswordRequest.setNewPassword("newPassword123!");

                when(profileService.resetPassword("nonexistent@example.com", "123456", "newPassword123!"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                mockMvc.perform(post("/profile/reset-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User not found"));

                verify(profileService).resetPassword("nonexistent@example.com", "123456", "newPassword123!");
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void changePassword_Success() throws Exception {
                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
                changePasswordRequest.setOldPassword("oldPassword123!");
                changePasswordRequest.setNewPassword("newPassword123!");

                when(profileService.changePassword("test@example.com", "oldPassword123!", "newPassword123!"))
                                .thenReturn(true);

                mockMvc.perform(post("/profile/change-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Password changed successfully"));

                verify(profileService).changePassword("test@example.com", "oldPassword123!", "newPassword123!");
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void changePassword_IncorrectOldPassword() throws Exception {
                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
                changePasswordRequest.setOldPassword("wrongPassword");
                changePasswordRequest.setNewPassword("newPassword123!");

                when(profileService.changePassword("test@example.com", "wrongPassword", "newPassword123!"))
                                .thenReturn(false);

                mockMvc.perform(post("/profile/change-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Incorrect old password"));

                verify(profileService).changePassword("test@example.com", "wrongPassword", "newPassword123!");
        }
}
