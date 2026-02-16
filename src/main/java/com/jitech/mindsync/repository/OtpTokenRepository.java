package com.jitech.mindsync.repository;

import com.jitech.mindsync.model.OtpToken;
import com.jitech.mindsync.model.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByEmailAndIsUsedFalse(String email);

    Optional<OtpToken> findByEmailAndOtpTypeAndIsUsedFalse(String email, OtpType otpType);

    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);

    void deleteByEmailAndOtpType(String email, OtpType otpType);
}
