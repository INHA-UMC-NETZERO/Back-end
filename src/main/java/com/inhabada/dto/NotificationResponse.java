package com.inhabada.dto;

import com.inhabada.entity.Notification;
import com.inhabada.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        Long relatedPostId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getRelatedPostId(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
