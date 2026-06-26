package com.inhabada.dto;

import com.inhabada.entity.RequestStatus;

import java.time.LocalDateTime;

public record MyRequestResponse(
        Long requestId,
        Long postId,
        String postTitle,
        Integer quantity,
        String requestedTime,
        RequestStatus status,
        LocalDateTime createdAt
) {
}
