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
        String token = extractToken(request);

        if (isPublicEndpoint(request)) {
            // Optionally resolve user if token is present, but don't require it
            if (token != null) {
                try {
                    Long userId = sessionService.validateSession(token);
                    request.setAttribute("userId", userId);
                } catch (UnauthorizedException ignored) {
                    // Public endpoint — ignore invalid token
                }
            }
            return true;
        }

        if (token == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }

        Long userId = sessionService.validateSession(token);
        request.setAttribute("userId", userId);

        return true;
    }

    /**
     * Authorization: Bearer 헤더를 우선 사용하고,
     * 헤더를 보낼 수 없는 SSE(EventSource) 연결을 위해 token 쿼리 파라미터로 폴백한다.
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isBlank()) {
            return tokenParam;
        }

        return null;
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
