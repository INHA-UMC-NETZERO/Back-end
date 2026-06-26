package com.inhabada.service;

import com.inhabada.dto.NotificationResponse;
import com.inhabada.entity.Notification;
import com.inhabada.entity.NotificationType;
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

    /**
     * 알림함을 열 때 호출. 해당 사용자의 안 읽은 알림을 모두 읽음 처리한다.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
