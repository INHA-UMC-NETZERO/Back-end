package com.inhabada.dto;

import com.inhabada.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MyRequestResponse(
        Long requestId,
        Long postId,
        String postTitle,
        @Schema(description = "신청한 게시글의 카테고리 한글 label", example = "식품")
        String postCategory,
        @Schema(description = "신청한 게시글의 서브카테고리 한글 label. 기타는 null일 수 있습니다.", example = "과자")
        String postSubCategory,
        String postLocation,
        Integer quantity,
        String requestedTime,
        @Schema(description = "신청 상태 한글 label", example = "신청중", allowableValues = {"신청중", "예약중", "완료", "거절됨"})
        RequestStatus status,
        LocalDateTime createdAt
) {
}
