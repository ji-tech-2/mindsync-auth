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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final GendersRepository gendersRepository;
    private final OccupationsRepository occupationsRepository;
    private final WorkRemotesRepository workRemotesRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public ProfileService(
            UserRepository userRepository,
            GendersRepository gendersRepository,
            OccupationsRepository occupationsRepository,
            WorkRemotesRepository workRemotesRepository,
            OtpService otpService,
            EmailService emailService,
            BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gendersRepository = gendersRepository;
        this.occupationsRepository = occupationsRepository;
        this.workRemotesRepository = workRemotesRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileResponse getProfile(String userId) {
        logger.info("Fetching profile for userId: {}", userId);
        Users user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    logger.error("Profile not found for userId: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        logger.debug("Profile retrieved successfully for userId: {}", user.getUserId());
        return new ProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getDob(),
                user.getGender() != null ? user.getGender().getGenderName() : null,
                user.getOccupation() != null ? user.getOccupation().getOccupationName() : null,
                user.getWorkRmt() != null ? user.getWorkRmt().getWorkRmtName() : null);
    }

    @Transactional
    public ProfileResponse updateProfile(String userId, ProfileUpdateRequest request) {
        logger.info("Starting profile update for userId: {}", userId);
        Users user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    logger.error("Profile update failed - User not found for userId: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        // Update name if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            logger.debug("Updating name for userId: {}", user.getUserId());
            user.setName(request.getName().trim());
        }

        // Update dob if provided
        if (request.getDob() != null) {
            logger.debug("Updating dob to: {} for userId: {}", request.getDob(), user.getUserId());
            user.setDob(request.getDob());
        }

        // Update gender if provided
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            logger.debug("Updating gender to: {} for userId: {}", request.getGender(), user.getUserId());
            Genders gender = gendersRepository.findByGenderName(request.getGender().trim())
                    .orElseThrow(() -> {
                        logger.error("Invalid gender provided: {}", request.getGender());
                        return new IllegalArgumentException("Invalid gender: " + request.getGender());
                    });
            user.setGender(gender);
        }

        // Update occupation if provided
        if (request.getOccupation() != null && !request.getOccupation().trim().isEmpty()) {
            logger.debug("Updating occupation to: {} for userId: {}", request.getOccupation(), user.getUserId());
            Occupations occupation = occupationsRepository.findByOccupationName(request.getOccupation().trim())
                    .orElseThrow(() -> {
                        logger.error("Invalid occupation provided: {}", request.getOccupation());
                        return new IllegalArgumentException("Invalid occupation: " + request.getOccupation());
                    });
            user.setOccupation(occupation);
        }

        // Update work remote status if provided
        if (request.getWorkRmt() != null && !request.getWorkRmt().trim().isEmpty()) {
            logger.debug("Updating work remote status to: {} for userId: {}", request.getWorkRmt(), user.getUserId());
            WorkRemotes workRmt = workRemotesRepository.findByWorkRmtName(request.getWorkRmt().trim())
                    .orElseThrow(
                            () -> {
                                logger.error("Invalid work remote status provided: {}", request.getWorkRmt());
                                return new IllegalArgumentException(
                                        "Invalid work remote status: " + request.getWorkRmt());
                            });
            user.setWorkRmt(workRmt);
        }

        Users savedUser = userRepository.save(user);
        logger.info("Profile updated successfully for userId: {}", savedUser.getUserId());

        return new ProfileResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getDob(),
                savedUser.getGender() != null ? savedUser.getGender().getGenderName() : null,
                savedUser.getOccupation() != null ? savedUser.getOccupation().getOccupationName() : null,
                savedUser.getWorkRmt() != null ? savedUser.getWorkRmt().getWorkRmtName() : null);
    }

    public void requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);
        // Verify user exists - silently return if not found to prevent user enumeration
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal whether user exists - just return silently
            logger.warn("Password reset requested for non-existent email (user enumeration attack prevention): {}",
                    email);
            return;
        }

        logger.debug("User found, initiating OTP send for email: {}", email);
        // Send OTP for password reset
        otpService.sendOtp(email, OtpType.PASSWORD_RESET);
        logger.info("Password reset OTP sent successfully for email: {}", email);
    }

    @Transactional
    public boolean resetPassword(String email, String otp, String newPassword) {
        logger.info("Password reset attempt for email: {}", email);

        // Validate password strength
        if (newPassword == null || newPassword.length() < 8) {
            logger.warn("Password reset failed - Password too short for email: {}", email);
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Validate and use OTP
        logger.debug("Validating OTP for email: {}", email);
        if (!otpService.validateAndUseOtp(email, otp, OtpType.PASSWORD_RESET)) {
            logger.warn("Password reset failed - Invalid or expired OTP for email: {}", email);
            return false;
        }

        // Find user and update password
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Password reset failed - User not found for email: {}", email);
                    return new IllegalArgumentException("User not found");
                });

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password reset successfully for userId: {}, email: {}", user.getUserId(), email);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(email);

        return true;
    }

    @Transactional
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        logger.info("Password change attempt for authenticated user with userId: {}", userId);

        // Validate password strength
        if (newPassword == null || newPassword.length() < 8) {
            logger.warn("Password change failed - Password too short for userId: {}", userId);
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Find user
        Users user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    logger.error("Password change failed - User not found for userId: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("Password change failed - Incorrect old password for userId: {}", userId);
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed successfully for userId: {}", user.getUserId());

        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail());

        return true;
    }
}
