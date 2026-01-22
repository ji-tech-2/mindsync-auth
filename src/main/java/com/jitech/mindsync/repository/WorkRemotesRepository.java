package com.jitech.mindsync.repository;

import com.jitech.mindsync.model.WorkRemotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorkRemotesRepository extends JpaRepository<WorkRemotes, Integer> {
    // Pastikan nama field di model WorkRemotes adalah 'workRmtName'
    Optional<WorkRemotes> findByWorkRmtName(String workRmtName);
}