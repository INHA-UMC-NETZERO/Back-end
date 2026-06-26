package com.inhabada.dto;

import com.inhabada.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String description,
        @Schema(description = "카테고리 한글 label", example = "식품")
        String category,
        @Schema(description = "서브카테고리 한글 label. 기타는 null일 수 있습니다.", example = "과자")
        String subCategory,
        List<String> imageUrls,
        Integer remainingQuantity,
        Integer totalQuantity,
        String location,
        Long giverId,
        String giverName,
        @Schema(description = "게시글 상태 한글 label", example = "나눔중", allowableValues = {"나눔중", "예약중", "마감"})
        PostStatus status,
        boolean closed,
        LocalDateTime createdAt,
        String availableTime
) {
}
