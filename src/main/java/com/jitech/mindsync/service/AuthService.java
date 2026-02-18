package com.jitech.mindsync.service;

import com.jitech.mindsync.dto.RegisterRequest;
import com.jitech.mindsync.model.Genders;
import com.jitech.mindsync.model.Occupations;
import com.jitech.mindsync.model.OtpType;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.model.WorkRemotes;
import com.jitech.mindsync.repository.GendersRepository;
import com.jitech.mindsync.repository.UserRepository;
import com.jitech.mindsync.repository.OccupationsRepository;
import com.jitech.mindsync.repository.WorkRemotesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final GendersRepository gendersRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OccupationsRepository occupationsRepository;
    private final WorkRemotesRepository workRemotesRepository;
    private final OtpService otpService;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            GendersRepository gendersRepository,
            OccupationsRepository occupationsRepository,
            WorkRemotesRepository workRemotesRepository,
            BCryptPasswordEncoder passwordEncoder,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.gendersRepository = gendersRepository;
        this.occupationsRepository = occupationsRepository;
        this.workRemotesRepository = workRemotesRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public Users registerUser(RegisterRequest request) {
        logger.info("Starting user registration process for email: {}", request.getEmail());

        // 1. Validate OTP
        logger.debug("Validating OTP for email: {}", request.getEmail());
        boolean otpValid = otpService.validateAndUseOtp(request.getEmail(), request.getOtp(), OtpType.SIGNUP);
        if (!otpValid) {
            logger.warn("Registration failed - Invalid or expired OTP for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        logger.debug("OTP validated successfully for email: {}", request.getEmail());

        // 2. Cek duplikasi email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed - Email already registered: {}", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        // 3. Cari Gender (Pastikan sesuai: Male, Female, Non-binary/Other)
        logger.debug("Validating gender: {}", request.getGender());
        Genders gender = gendersRepository.findByGenderName(request.getGender().trim())
                .orElseThrow(() -> {
                    logger.error("Invalid gender provided: {}", request.getGender());
                    return new IllegalArgumentException("Invalid gender");
                });

        // 4. Cari Occupation (Sesuai tabel: Employed, Student, dsb.)
        String occName = (request.getOccupation() == null) ? "Student" : request.getOccupation().trim();
        logger.debug("Validating occupation: {}", occName);
        Occupations occupation = occupationsRepository.findByOccupationName(occName)
                .orElseThrow(() -> {
                    logger.error("Invalid occupation provided: {}", occName);
                    return new IllegalArgumentException("Invalid occupation: " + occName);
                });

        // 5. Cari Work Remote (PENTING: Gunakan 'In-person', bukan 'On-site')
        String workRmtName = (request.getWorkRmt() == null) ? "In-person" : request.getWorkRmt().trim();
        logger.debug("Validating work remote status: {}", workRmtName);
        WorkRemotes workRmt = workRemotesRepository.findByWorkRmtName(workRmtName)
                .orElseThrow(() -> {
                    logger.error("Invalid work remote status provided: {}", workRmtName);
                    return new IllegalArgumentException("Invalid work remote status: " + workRmtName);
                });

        // 6. Simpan User
        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setDob(request.getDob());
        user.setGender(gender);
        user.setOccupation(occupation);
        user.setWorkRmt(workRmt);

        Users savedUser = userRepository.save(user);
        logger.info("User registration successful - userId: {}, email: {}", savedUser.getUserId(),
                savedUser.getEmail());

        return savedUser;
    }

    public Users login(String email, String password) {
        logger.info("Login attempt for email: {}", email);
        Optional<Users> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                logger.info("Login successful for userId: {}, email: {}", user.getUserId(), email);
                return user;
            } else {
                logger.warn("Login failed - Invalid password for email: {}", email);
            }
        } else {
            logger.warn("Login failed - User not found for email: {}", email);
        }
        return null;
    }
}