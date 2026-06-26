package com.inhabada.dto;

import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;

import java.time.LocalDateTime;

public record RequestResponse(
        Long id,
        Long postId,
        Long receiverId,
        Long slotId,
        Integer quantity,
        RequestStatus status,
        LocalDateTime createdAt
) {
    public static RequestResponse from(ShareRequest request) {
        return new RequestResponse(
                request.getId(),
                request.getPostId(),
                request.getReceiverId(),
                request.getSlotId(),
                request.getQuantity(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
