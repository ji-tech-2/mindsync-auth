package com.jitech.mindsync.repository;

import com.jitech.mindsync.model.Genders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GendersRepository extends JpaRepository<Genders, Integer> {
    Optional<Genders> findByGenderName(String genderName);
}