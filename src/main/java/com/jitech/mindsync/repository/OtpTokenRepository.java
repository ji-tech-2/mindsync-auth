package com.jitech.mindsync.repository;

import com.jitech.mindsync.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    Optional<OtpToken> findByEmailAndOtpCodeAndIsUsedFalse(String email, String otpCode);
    
    Optional<OtpToken> findByEmailAndOtpCodeAndIsVerifiedTrueAndIsUsedFalse(String email, String otpCode);
    
    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);
    
    void deleteByEmail(String email);
}
