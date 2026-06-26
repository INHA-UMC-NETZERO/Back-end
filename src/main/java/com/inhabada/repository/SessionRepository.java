package com.inhabada.repository;

import com.inhabada.entity.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
