package com.inhabada.service;

import com.inhabada.entity.Session;
import com.inhabada.exception.UnauthorizedException;
import com.inhabada.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final int ttlDays;

    public SessionService(SessionRepository sessionRepository,
                          @Value("${app.session.ttl-days:7}") int ttlDays) {
        this.sessionRepository = sessionRepository;
        this.ttlDays = ttlDays;
    }

    @Transactional
    public String createSession(Long userId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(ttlDays);
        Session session = new Session(userId, token, expiresAt);
        sessionRepository.save(session);
        return token;
    }

    @Transactional(readOnly = true)
    public Long validateSession(String token) {
        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요");
        }

        return session.getUserId();
    }

    @Transactional
    public void deleteSession(String token) {
        sessionRepository.deleteByToken(token);
    }

    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void cleanExpiredSessions() {
        sessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
