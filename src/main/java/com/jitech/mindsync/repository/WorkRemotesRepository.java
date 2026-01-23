package com.jitech.mindsync.repository;

import com.jitech.mindsync.model.WorkRemotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorkRemotesRepository extends JpaRepository<WorkRemotes, Integer> {
    Optional<WorkRemotes> findByWorkRmtName(String workRmtName);
}