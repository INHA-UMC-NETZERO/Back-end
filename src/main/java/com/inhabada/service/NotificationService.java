package com.inhabada.service;

import com.inhabada.dto.NotificationResponse;
import com.inhabada.entity.Notification;
import com.inhabada.entity.NotificationType;
import com.inhabada.exception.ForbiddenException;
import com.inhabada.exception.NotFoundException;
import com.inhabada.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEmitterRegistry emitterRegistry;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationEmitterRegistry emitterRegistry) {
        this.notificationRepository = notificationRepository;
        this.emitterRegistry = emitterRegistry;
    }

    @Transactional
    public void createNotification(Long userId, NotificationType type, String message, Long relatedPostId) {
        Notification notification = new Notification(userId, type, message, relatedPostId);
        Notification saved = notificationRepository.save(notification);

        // 연결돼 있으면 실시간 전송 (없으면 무시 — 다음 조회 시 GET으로 복구)
        emitterRegistry.send(userId, "notification", NotificationResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없습니다"));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenException("권한이 없습니다");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
