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
        @Schema(description = "완료된 요청에서 신청자에게 적립된 탄소 저감량. 완료 전에는 null이며 단위는 gram입니다.", example = "110")
        Long carbonSavingGram,
        @Schema(description = "전달 완료 처리 시각. 완료 전에는 null입니다.", example = "2026-06-27T15:30:00")
        LocalDateTime completedAt,
        @Schema(description = "신청 상태 한글 label", example = "신청중", allowableValues = {"신청중", "예약중", "완료", "거절됨"})
        RequestStatus status,
        LocalDateTime createdAt
) {
}
