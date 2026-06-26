package com.inhabada.service;

import com.inhabada.dto.LoginRequest;
import com.inhabada.dto.LoginResponse;
import com.inhabada.entity.User;
import com.inhabada.exception.ValidationException;
import com.inhabada.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ALLOWED_DOMAIN = "@inha.edu";

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        validateEmailDomain(request.email());

        // 목업: 이메일로 기존 사용자 조회, 없으면 자동 생성
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> userRepository.save(new User(request.email(), request.nickname())));

        // UUID 기반 세션 토큰 발급
        String token = sessionService.createSession(user.getId());

        return new LoginResponse(token, user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public void logout(String token) {
        sessionService.deleteSession(token);
    }

    private void validateEmailDomain(String email) {
        if (email == null || !email.toLowerCase().endsWith(ALLOWED_DOMAIN)) {
            throw new ValidationException("@inha.edu 이메일만 사용할 수 있습니다");
        }
    }
}
