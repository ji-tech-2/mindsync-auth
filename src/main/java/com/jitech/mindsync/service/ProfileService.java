package com.jitech.mindsync.service;

import com.jitech.mindsync.dto.ProfileResponse;
import com.jitech.mindsync.dto.ProfileUpdateRequest;
import com.jitech.mindsync.model.Genders;
import com.jitech.mindsync.model.Occupations;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.model.WorkRemotes;
import com.jitech.mindsync.repository.GendersRepository;
import com.jitech.mindsync.repository.OccupationsRepository;
import com.jitech.mindsync.repository.UserRepository;
import com.jitech.mindsync.repository.WorkRemotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {

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

    public ProfileResponse getProfile(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update name if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        // Update gender if provided
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            Genders gender = gendersRepository.findByGenderName(request.getGender().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid gender: " + request.getGender()));
            user.setGender(gender);
        }

        // Update occupation if provided
        if (request.getOccupation() != null && !request.getOccupation().trim().isEmpty()) {
            Occupations occupation = occupationsRepository.findByOccupationName(request.getOccupation().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid occupation: " + request.getOccupation()));
            user.setOccupation(occupation);
        }

        // Update work remote status if provided
        if (request.getWorkRmt() != null && !request.getWorkRmt().trim().isEmpty()) {
            WorkRemotes workRmt = workRemotesRepository.findByWorkRmtName(request.getWorkRmt().trim())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Invalid work remote status: " + request.getWorkRmt()));
            user.setWorkRmt(workRmt);
        }

        Users savedUser = userRepository.save(user);

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
        // Verify user exists - silently return if not found to prevent user enumeration
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal whether user exists - just return silently
            return;
        }

        // Send OTP
        otpService.sendOtp(email);
    }

    @Transactional
    public boolean changePassword(String email, String otp, String newPassword) {
        // Validate password strength
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Validate and use OTP
        if (!otpService.validateAndUseOtp(email, otp)) {
            return false;
        }

        // Find user and update password
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(email);

        return true;
    }
}
