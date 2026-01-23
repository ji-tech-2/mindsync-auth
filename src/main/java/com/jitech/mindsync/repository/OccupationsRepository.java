package com.jitech.mindsync.repository;

import com.jitech.mindsync.model.Occupations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface OccupationsRepository extends JpaRepository<Occupations, Integer> {
    Optional<Occupations> findByOccupationName(String occupationName);
}