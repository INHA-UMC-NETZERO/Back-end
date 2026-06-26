package com.inhabada.dto;

import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record RequestResponse(
        Long id,
        Long postId,
        Long receiverId,
        String requestedTime,
        Integer quantity,
        @Schema(description = "신청 상태 한글 label", example = "신청중", allowableValues = {"신청중", "예약중", "완료", "거절됨"})
        RequestStatus status,
        LocalDateTime createdAt
) {
    public static RequestResponse from(ShareRequest request) {
        return new RequestResponse(
                request.getId(),
                request.getPostId(),
                request.getReceiverId(),
                request.getRequestedTime(),
                request.getQuantity(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
