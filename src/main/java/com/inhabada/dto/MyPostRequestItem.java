package com.inhabada.dto;

import com.inhabada.entity.RequestStatus;

import java.time.LocalDateTime;

public record MyPostRequestItem(
        Long requestId,
        Long receiverId,
        String receiverName,
        Integer quantity,
        String requestedTime,
        RequestStatus status,
        LocalDateTime createdAt
) {
}
