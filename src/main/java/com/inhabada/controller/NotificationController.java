package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.NotificationResponse;
import com.inhabada.dto.PageResponse;
import com.inhabada.service.NotificationEmitterRegistry;
import com.inhabada.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "사용자 알림 조회와 읽음 처리 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationEmitterRegistry emitterRegistry;

    public NotificationController(NotificationService notificationService,
                                 NotificationEmitterRegistry emitterRegistry) {
        this.notificationService = notificationService;
        this.emitterRegistry = emitterRegistry;
    }

    /**
     * 실시간 알림 스트림 (SSE).
     * 브라우저 EventSource는 헤더를 보낼 수 없으므로 토큰을 쿼리 파라미터로 받는다:
     *   GET /api/notifications/stream?token={세션토큰}
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@CurrentUser Long userId) {
        return emitterRegistry.subscribe(userId);
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "인증된 사용자의 알림 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @CurrentUser Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> page = notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(PageResponse.from(page, n -> n));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<Void> markAsRead(@CurrentUser Long userId,
                                           @Parameter(description = "읽음 처리할 알림 ID")
                                           @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }
}
