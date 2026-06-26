package com.inhabada.dto;

import com.inhabada.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MyPostRequestItem(
        Long requestId,
        Long receiverId,
        String receiverName,
        Integer quantity,
        String requestedTime,
        @Schema(description = "신청 상태 한글 label", example = "신청중", allowableValues = {"신청중", "예약중", "완료", "거절됨"})
        RequestStatus status,
        LocalDateTime createdAt
) {
}
