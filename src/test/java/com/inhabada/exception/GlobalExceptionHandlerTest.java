package com.inhabada.exception;

import com.inhabada.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationException_withFields_returns400() {
        ValidationException ex = new ValidationException("입력값이 올바르지 않습니다", List.of("title", "quantity"));

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("입력값이 올바르지 않습니다");
        assertThat(response.getBody().fields()).containsExactly("title", "quantity");
    }

    @Test
    void handleValidationException_withoutFields_returns400() {
        ValidationException ex = new ValidationException("입력값이 올바르지 않습니다");

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().fields()).isNull();
    }

    @Test
    void handleMethodArgumentNotValid_returns400WithFields() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "title", "제목은 필수입니다");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().fields()).contains("title");
        assertThat(response.getBody().message()).isEqualTo("제목은 필수입니다");
    }

    @SuppressWarnings("unchecked")
    @Test
    void handleConstraintViolation_returns400() {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("keyword");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("키워드는 1~20자여야 합니다");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().fields()).contains("keyword");
    }

    @Test
    void handleHttpMessageNotReadable_returns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("요청 본문을 읽을 수 없습니다");
    }

    @Test
    void handleUnauthorizedException_returns401() {
        UnauthorizedException ex = new UnauthorizedException("로그인이 필요합니다");
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<?> response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.error()).isEqualTo("UNAUTHORIZED");
        assertThat(body.message()).isEqualTo("로그인이 필요합니다");
    }

    @Test
    void handleUnauthorizedException_forEventStream_returns401WithoutBody() {
        UnauthorizedException ex = new UnauthorizedException("세션이 만료되었습니다");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Accept")).thenReturn("text/event-stream");

        ResponseEntity<?> response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleForbiddenException_returns403() {
        ForbiddenException ex = new ForbiddenException("권한이 없습니다");

        ResponseEntity<ErrorResponse> response = handler.handleForbiddenException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("FORBIDDEN");
        assertThat(response.getBody().message()).isEqualTo("권한이 없습니다");
    }

    @Test
    void handleNotFoundException_returns404() {
        NotFoundException ex = new NotFoundException("게시글을 찾을 수 없습니다");

        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("게시글을 찾을 수 없습니다");
    }

    @Test
    void handleConflictException_returns409() {
        ConflictException ex = new ConflictException("잔여 수량을 초과하는 요청입니다");

        ResponseEntity<ErrorResponse> response = handler.handleConflictException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("CONFLICT");
        assertThat(response.getBody().message()).isEqualTo("잔여 수량을 초과하는 요청입니다");
    }

    @Test
    void handleOptimisticLockException_returns409() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Post", 1L);

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("CONFLICT");
        assertThat(response.getBody().message()).contains("동시 수정 충돌");
    }

    @Test
    void handleDataIntegrityViolation_returns409() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Unique constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("CONFLICT");
        assertThat(response.getBody().message()).contains("중복된 데이터");
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("서버 오류가 발생했습니다");
    }
}
