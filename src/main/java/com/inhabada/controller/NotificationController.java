package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.NotificationResponse;
import com.inhabada.dto.PageResponse;
import com.inhabada.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @CurrentUser Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> page = notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(PageResponse.from(page, n -> n));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@CurrentUser Long userId, @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }
}
