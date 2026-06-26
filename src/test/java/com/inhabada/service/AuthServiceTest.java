package com.inhabada.service;

import com.inhabada.dto.LoginRequest;
import com.inhabada.dto.LoginResponse;
import com.inhabada.entity.User;
import com.inhabada.exception.ValidationException;
import com.inhabada.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionService sessionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, sessionService);
    }

    @Test
    void login_shouldSucceedWithInhaEduEmail() {
        String email = "student@inha.edu";
        String nickname = "학생";
        User user = new User(email, nickname);
        // Use reflection to set id since it's generated
        setUserId(user, 1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(sessionService.createSession(1L)).thenReturn("test-token-uuid");

        LoginResponse response = authService.login(new LoginRequest(email, nickname));

        assertThat(response.token()).isEqualTo("test-token-uuid");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.nickname()).isEqualTo(nickname);
    }

    @Test
    void login_shouldCreateNewUserIfNotExists() {
        String email = "newstudent@inha.edu";
        String nickname = "신입생";
        User newUser = new User(email, nickname);
        setUserId(newUser, 2L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(sessionService.createSession(2L)).thenReturn("new-token-uuid");

        LoginResponse response = authService.login(new LoginRequest(email, nickname));

        assertThat(response.token()).isEqualTo("new-token-uuid");
        assertThat(response.userId()).isEqualTo(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_shouldRejectNonInhaEduEmail() {
        String email = "user@gmail.com";

        assertThatThrownBy(() -> authService.login(new LoginRequest(email, "닉네임")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("@inha.edu");

        verify(userRepository, never()).findByEmail(any());
        verify(sessionService, never()).createSession(anyLong());
    }

    @Test
    void login_shouldRejectNullEmail() {
        assertThatThrownBy(() -> authService.login(new LoginRequest(null, "닉네임")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("@inha.edu");
    }

    @Test
    void login_shouldBeCaseInsensitiveForDomain() {
        String email = "student@INHA.EDU";
        User user = new User(email, "학생");
        setUserId(user, 1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(sessionService.createSession(1L)).thenReturn("token");

        LoginResponse response = authService.login(new LoginRequest(email, "학생"));

        assertThat(response.token()).isEqualTo("token");
    }

    @Test
    void logout_shouldDeleteSession() {
        authService.logout("some-token");
        verify(sessionService).deleteSession("some-token");
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
