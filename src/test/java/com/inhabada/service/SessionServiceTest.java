package com.inhabada.service;

import com.inhabada.entity.Session;
import com.inhabada.exception.UnauthorizedException;
import com.inhabada.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(sessionRepository, 7);
    }

    @Test
    void createSession_shouldGenerateTokenAndSaveSession() {
        Long userId = 1L;
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        String token = sessionService.createSession(userId);

        assertThat(token).isNotNull().isNotEmpty();

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());

        Session savedSession = captor.getValue();
        assertThat(savedSession.getUserId()).isEqualTo(userId);
        assertThat(savedSession.getToken()).isEqualTo(token);
        assertThat(savedSession.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
    }

    @Test
    void validateSession_shouldReturnUserIdForValidSession() {
        String token = "valid-token";
        Session session = new Session(42L, token, LocalDateTime.now().plusDays(3));
        when(sessionRepository.findByToken(token)).thenReturn(Optional.of(session));

        Long userId = sessionService.validateSession(token);

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void validateSession_shouldThrowForNonExistentToken() {
        when(sessionRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.validateSession("invalid"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("세션이 만료되었습니다");
    }

    @Test
    void validateSession_shouldThrowForExpiredSession() {
        String token = "expired-token";
        Session session = new Session(1L, token, LocalDateTime.now().minusDays(1));
        when(sessionRepository.findByToken(token)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.validateSession(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("세션이 만료되었습니다");
    }

    @Test
    void deleteSession_shouldCallRepositoryDelete() {
        String token = "token-to-delete";

        sessionService.deleteSession(token);

        verify(sessionRepository).deleteByToken(token);
    }

    @Test
    void cleanExpiredSessions_shouldDeleteExpiredSessions() {
        sessionService.cleanExpiredSessions();

        verify(sessionRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
