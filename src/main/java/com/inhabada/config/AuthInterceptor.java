package com.inhabada.config;

import com.inhabada.exception.UnauthorizedException;
import com.inhabada.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (isPublicEndpoint(request)) {
            // Optionally resolve user if token is present, but don't require it
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    Long userId = sessionService.validateSession(token);
                    request.setAttribute("userId", userId);
                } catch (UnauthorizedException ignored) {
                    // Public endpoint — ignore invalid token
                }
            }
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }

        String token = authHeader.substring(7);
        Long userId = sessionService.validateSession(token);
        request.setAttribute("userId", userId);

        return true;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // GET /api/posts (feed listing) is public
        if ("GET".equalsIgnoreCase(method) && uri.matches("/api/posts(/\\d+)?")) {
            return true;
        }

        return false;
    }
}
