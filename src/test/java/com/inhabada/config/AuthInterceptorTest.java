package com.inhabada.config;

import com.inhabada.exception.UnauthorizedException;
import com.inhabada.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor(sessionService);
    }

    @Test
    void preHandle_shouldPassWithValidToken() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(sessionService.validateSession("valid-token")).thenReturn(1L);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(request).setAttribute("userId", 1L);
    }

    @Test
    void preHandle_shouldThrowWhenNoAuthHeader() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("로그인이 필요합니다");
    }

    @Test
    void preHandle_shouldThrowWhenInvalidBearerFormat() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("로그인이 필요합니다");
    }

    @Test
    void preHandle_shouldAllowGetPostsWithoutAuth() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldAllowGetPostDetailWithoutAuth() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/posts/123");
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldAllowCorsPreflightWithoutAuth() {
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/posts");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldRequireAuthForPostPosts() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void preHandle_shouldResolveUserOnPublicEndpointWithValidToken() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(sessionService.validateSession("valid-token")).thenReturn(5L);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(request).setAttribute("userId", 5L);
    }

    @Test
    void preHandle_shouldIgnoreInvalidTokenOnPublicEndpoint() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/posts");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(sessionService.validateSession("invalid-token")).thenThrow(new UnauthorizedException("expired"));

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }
}
